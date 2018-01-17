package com.dubiel.sample.googlebookviewerrx;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.Collections;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.HasFragmentInjector;
import dagger.android.support.DaggerAppCompatActivity;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MainActivity extends DaggerAppCompatActivity implements HasFragmentInjector,
        DrawerListAdapter.OnDrawerItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    static final public int MAX_RESULTS = 40;
    static final private int CACHE_MAX_SIZE = 3;

    @Inject
    GoogleBooksClient googleBooksClient;

    @NonNull
    private final PublishSubject<Void> updateSubject = PublishSubject.create();

    @NonNull
    private final Cache<Integer, BookListItems> bookListItemsCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .removalListener(new RemovalListener<Integer, BookListItems>() {
                public void onRemoval(RemovalNotification<Integer, BookListItems> removalNotification) {
                    System.out.println("key removed: " + removalNotification.getKey());
                    removeFromBookListItemAdapater(removalNotification.getKey());
//                        updateBookItemListAdapter();
                }
            })
            .build();

    private Subscription subscription;
    private BookItemListAdapter bookItemListAdapter;
    private RecyclerView drawerList;
    private RecyclerView recyclerView;
    private String currentSearchTerm = "cats";
    private Integer currentStartIndex = 0;
    private ProgressBar spinner;
    private Boolean cacheLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);

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

        recyclerView = (RecyclerView)findViewById(R.id.book_item_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        // @Todo, implement RxBinding for RecylcerView or better scrolling solution
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!cacheLoading) {
                    if (!recyclerView.canScrollVertically(-1)) {
                        System.out.println("!recyclerView.canScrollVertically(-1)");
//                        int minKey = Collections.min(bookListItemsCache.asMap().keySet());
//                        if(minKey > 0) {
//                            spinner.setVisibility(View.VISIBLE);
//                            currentStartIndex = (minKey - 1) * MAX_RESULTS;
//                            update();
//                        }
                    } else if (!recyclerView.canScrollVertically(1)) {
                        int maxKey = Collections.max(bookListItemsCache.asMap().keySet());
                        spinner.setVisibility(View.VISIBLE);
                        currentStartIndex = (maxKey + 1) * MAX_RESULTS;
                        update();
                    }
                }
            }
        });

//        bookItemListAdapter = new BookItemListAdapter(getApplicationContext(), bookListItemsCache);
        bookItemListAdapter = new BookItemListAdapter(getApplicationContext());
        recyclerView.setAdapter(bookItemListAdapter);

        spinner.setVisibility(View.VISIBLE);
        subscription = Observable
                .defer(() -> Observable.just(getSearchData(currentSearchTerm, currentStartIndex)))
                .flatMap(data -> googleBooksClient.getBooks(data))
                .repeatWhen(repeatHandler ->
                        repeatHandler.flatMap(nothing -> updateSubject.asObservable()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    int cacheKey = currentStartIndex / MAX_RESULTS;
                    bookListItemsCache.put(cacheKey, result);
                    addToBookListItemAdapter(result);
                    spinner.setVisibility(View.GONE);
                    cacheLoading = false;
                }, err -> {
                    Log.i(TAG, err.getMessage());
                    cacheLoading = false;
                });
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
        int id = item.getItemId();
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

        search();
    }

    private void update() {
        updateSubject.onNext(null);
    }

    private GoogleBooksParameters getSearchData(String searchTerm, Integer startIndex) {
        return new GoogleBooksParameters(getApplicationContext().getResources().getString(R.string.google_books_api_key),
                searchTerm, startIndex, MAX_RESULTS);
    }

    private void search() {
        spinner.setVisibility(View.VISIBLE);
        bookListItemsCache.invalidateAll();
        currentStartIndex = 0;
        update();
    }

    private void removeFromBookListItemAdapater(int key) {
        bookItemListAdapter.removeKey(key);
        recyclerView.scrollToPosition(76);
    }

    private void addToBookListItemAdapter(BookListItems bookListItems) {
//        if(bookListItemsCache.asMap().keySet().size() == 0) {
////            bookItemListAdapter.setItemCount(0);
//            return;
//        }

        bookItemListAdapter.add(bookListItems);

//        int maxKey = Collections.max(bookListItemsCache.asMap().keySet());
//        int itemCount = maxKey * MAX_RESULTS;
//        BookListItems bookListItems = bookListItemsCache.getIfPresent(maxKey);
//        if(bookListItems instanceof BookListItems) {
//            itemCount += bookListItems.getItems().length;
//        }

//        bookItemListAdapter.setItemCount(itemCount);
//        bookItemListAdapter.notifyDataSetChanged();
    }
}
