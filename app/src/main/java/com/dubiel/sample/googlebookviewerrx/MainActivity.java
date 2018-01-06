package com.dubiel.sample.googlebookviewerrx;

import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.dubiel.sample.googlebookviewerrx.data.BookListItems;
import com.dubiel.sample.googlebookviewerrx.viewadapter.BookItemListAdapter;
import com.dubiel.sample.googlebookviewerrx.viewadapter.DrawerListAdapter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements
        DrawerListAdapter.OnDrawerItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Subscription subscription;

    static final private int CACHE_MAX_SIZE = 5;

    private Cache<Integer, BookListItems> bookListItemsCache;

    private BookItemListAdapter bookItemListAdapter;
    private RecyclerView drawerList;
    private String currentSearchTerm;
    private ProgressBar spinner;
    private Boolean cacheLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (RecyclerView) findViewById(R.id.left_drawer);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        drawerList.setLayoutManager(linearLayoutManager);
        drawerList.setHasFixedSize(true);

        String[] popularSearchTerms = getResources().getStringArray(R.array.category_array);
        currentSearchTerm = popularSearchTerms[0];

        drawerList.setAdapter(new DrawerListAdapter(popularSearchTerms, this));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(R.string.popular_searches);
                invalidateOptionsMenu();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.book_item_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!cacheLoading) {
//                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//                    if (dy < 0) {
//                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
//                        int cacheKey = (int)Math.floor((firstVisibleItemPosition - 1) / SearchManager.MAX_RESULTS);
//                        if(!(bookListItemsCache.getIfPresent(cacheKey) instanceof BookListItems)) {
//                            updateCache(firstVisibleItemPosition - 1);
//                        }
//                    } else if (dy > 0) {
//                        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
//                        int cacheKey = (int)Math.floor((lastVisibleItemPosition + 1) / SearchManager.MAX_RESULTS);
//                        if(!(bookListItemsCache.getIfPresent(cacheKey) instanceof BookListItems)) {
//                            updateCache(lastVisibleItemPosition + 1);
//                        }
//                    }
                }
            }
        });

        bookListItemsCache = CacheBuilder.newBuilder()
                .maximumSize(CACHE_MAX_SIZE)
                .removalListener(new RemovalListener<Integer, BookListItems>() {
                    public void onRemoval(RemovalNotification<Integer, BookListItems> removalNotification) {
//                        updateBookItemListAdapterItemCount();
                    }
                })
                .build();

        bookItemListAdapter = new BookItemListAdapter(getApplicationContext(), bookListItemsCache);
        recyclerView.setAdapter(bookItemListAdapter);

        Observer<BookListItems> observer = new Observer<BookListItems>() {
            @Override
            public void onCompleted() {
                System.out.println("All data emitted.");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Error received: " + e.getMessage());
            }

            @Override public void onNext(BookListItems books) {
                System.out.println("In onNext()");
                System.out.println("BookListItems length: " + books.getItems().length);
            }
        };

        Subscription subscription = observable
                .subscribeOn(Schedulers.io())       //observable will run on IO thread.
                .observeOn(AndroidSchedulers.mainThread())      //Observer will run on main thread.
                .subscribe(observer);
    }

    @Override protected void onDestroy() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        android.app.SearchManager searchManager = (android.app.SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchTerm = query;
                search();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemClick(View view, int position) {
        String categoryString = getResources().getStringArray(R.array.category_array)[position];

        currentSearchTerm = categoryString;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void onResultsReady(boolean resetScrollPosition) {
        cacheLoading = false;
        spinner.setVisibility(View.GONE);
        bookItemListAdapter.notifyDataSetChanged();

        if(resetScrollPosition) {
            ((RecyclerView) findViewById(R.id.book_item_list_recycler_view)).scrollToPosition(0);
        }
    }

    private void search() {
        spinner.setVisibility(View.VISIBLE);
        bookListItemsCache.invalidateAll();

        subscription = GoogleBooksClient.getInstance()
                .getBooks("AIzaSyBaTPJ5YXt5V6VSuvnxhIgj4NJZ1vJqtmM",
                        "cats",
                        0,
                        40)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BookListItems>() {
                    @Override public void onCompleted() {
                        System.out.println("In onCompleted()");
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        System.out.println("In onError()");
                        System.out.println(e.getCause());
                    }

                    @Override public void onNext(BookListItems books) {
                        System.out.println("In onNext()");
                        System.out.println("BookListItems length: " + books.getItems().length);
                    }
                });
    }

    private void updateCache(int key) {
        if(cacheLoading) {
            return;
        }

        cacheLoading = true;
        int cacheKey = (int)Math.floor(key / 40);

        if(bookListItemsCache.getIfPresent(cacheKey) instanceof BookListItems) {
            cacheLoading = false;
            spinner.setVisibility(View.GONE);
            return;
        }

        spinner.setVisibility(View.VISIBLE);
    }

    private void updateBookItemListAdapterItemCount() {
        if(bookListItemsCache.asMap().keySet().size() == 0) {
            bookItemListAdapter.setItemCount(0);
            return;
        }

        int maxKey = Collections.max(bookListItemsCache.asMap().keySet());
//        int itemCount = maxKey * SearchManager.MAX_RESULTS;
        int itemCount = 0;
        BookListItems bookListItems = bookListItemsCache.getIfPresent(maxKey);
        if(bookListItems instanceof BookListItems) {
            itemCount += bookListItems.getItems().length;
        }

        bookItemListAdapter.setItemCount(itemCount);
    }
}
