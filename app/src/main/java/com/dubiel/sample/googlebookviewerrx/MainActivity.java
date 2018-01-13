package com.dubiel.sample.googlebookviewerrx;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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
import javax.inject.Singleton;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
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
    static final private int CACHE_MAX_SIZE = 5;

    @Inject
    GoogleBooksClient googleBooksClient;

    @NonNull
    private final PublishSubject<Void> updateSubject = PublishSubject.create();

    @NonNull
    private final Cache<Integer, BookListItems> bookListItemsCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .removalListener(new RemovalListener<Integer, BookListItems>() {
                public void onRemoval(RemovalNotification<Integer, BookListItems> removalNotification) {
                        updateBookItemListAdapterItemCount();
                }
            })
            .build();

    private Subscription subscription;
    private BookItemListAdapter bookItemListAdapter;
    private RecyclerView drawerList;
    private String currentSearchTerm = "cats";
    private Integer currentStartIndex = 0;
    private ProgressBar spinner;
    private Boolean cacheLoading = false;

    private void load() {
        subscription = Observable
                .defer(() -> Observable.just(getSearchData(currentSearchTerm, currentStartIndex)))
                .flatMap(data -> googleBooksClient.getBooks(data))
                .repeatWhen(repeatHandler ->
                        repeatHandler.flatMap(nothing -> updateSubject.asObservable()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
//                    System.out.println(result.getItems()[0].getVolumeInfo().getTitle());
                    int cacheKey = currentStartIndex / MAX_RESULTS;
                    bookListItemsCache.put(cacheKey, result);
                    updateBookItemListAdapterItemCount();
                    spinner.setVisibility(View.GONE);
                    bookItemListAdapter.notifyDataSetChanged();
                    cacheLoading = false;
                }, err -> {
                    System.out.println(err);
                    Log.i(TAG, err.getMessage());
                    cacheLoading = false;
                });
    }

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

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.book_item_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!cacheLoading) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (dy < 0) {
//                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
//                        int cacheKey = (int)Math.floor((firstVisibleItemPosition - 1) / SearchManager.MAX_RESULTS);
//                        if(!(bookListItemsCache.getIfPresent(cacheKey) instanceof BookListItems)) {
//                            updateCache(firstVisibleItemPosition - 1);
//                        }
                    } else if (dy > 0) {
                        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                        int cacheKey = (int)Math.floor((lastVisibleItemPosition + 1) / MAX_RESULTS);
//                        System.out.println("cacheKey: " + cacheKey);
                        if(!(bookListItemsCache.getIfPresent(cacheKey) instanceof BookListItems)) {
                            updateCache(lastVisibleItemPosition + 1);
                        }
                    }
                }
            }
        });

        bookItemListAdapter = new BookItemListAdapter(getApplicationContext(), bookListItemsCache);
        recyclerView.setAdapter(bookItemListAdapter);

//        subscription = Observable
//                .defer(() -> Observable.just(getSearchData()))
//                .flatMap(data -> GoogleBooksClient.getInstance().getBooks(data))
//                .subscribeOn(Schedulers.io())
//                .subscribe(System.out::println);

//        final Button buttonSearch = (Button) findViewById(R.id.button_search);
//        buttonSearch.setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                currentStartIndex += 40;
//                System.out.println("currentStartIndex: " + currentStartIndex);
//                update();
//            }
//        });

        load();
    }

    private void update() {
        updateSubject.onNext(null);
    }

    private GoogleBooksParameters getSearchData(String searchTerm, Integer startIndex) {
        return new GoogleBooksParameters(getApplicationContext().getResources().getString(R.string.google_books_api_key),
                searchTerm, startIndex, 40);
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

//    public void onResultsReady(boolean resetScrollPosition) {
//        cacheLoading = false;
//        spinner.setVisibility(View.GONE);
//        bookItemListAdapter.notifyDataSetChanged();
//
//        if(resetScrollPosition) {
//            ((RecyclerView) findViewById(R.id.book_item_list_recycler_view)).scrollToPosition(0);
//        }
//    }

    private void search() {
//        spinner.setVisibility(View.VISIBLE);
        bookListItemsCache.invalidateAll();
    }

    private void updateCache(int key) {
        if(cacheLoading) {
            return;
        }

        cacheLoading = true;

        int cacheKey = (int)Math.floor(key / MAX_RESULTS);
        if(bookListItemsCache.getIfPresent(cacheKey) instanceof BookListItems) {
            cacheLoading = false;
            spinner.setVisibility(View.GONE);
            return;
        }

        spinner.setVisibility(View.VISIBLE);

        currentStartIndex += MAX_RESULTS;
        update();
    }

    private void updateBookItemListAdapterItemCount() {
        if(bookListItemsCache.asMap().keySet().size() == 0) {
            bookItemListAdapter.setItemCount(0);
            return;
        }

        int maxKey = Collections.max(bookListItemsCache.asMap().keySet());
        int itemCount = maxKey * MAX_RESULTS;
        BookListItems bookListItems = bookListItemsCache.getIfPresent(maxKey);
        if(bookListItems instanceof BookListItems) {
            itemCount += bookListItems.getItems().length;
        }

        bookItemListAdapter.setItemCount(itemCount);
    }

}
