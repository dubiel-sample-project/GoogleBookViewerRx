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
import android.widget.Toast;

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

    enum SCROLL_STATUS {SCROLLING_UP, SCROLLING_DOWN};

    static final public int MAX_RESULTS = 40;
    static final public int CACHE_MAX_SIZE = 3;

    @Inject
    GoogleBooksClient googleBooksClient;

    @NonNull
    private final PublishSubject<Void> updateSubject = PublishSubject.create();

    @NonNull
    private final Cache<Integer, BookListItems> bookListItemsCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .removalListener(new RemovalListener<Integer, BookListItems>() {
                public void onRemoval(RemovalNotification<Integer, BookListItems> removalNotification) {
                    System.out.println("onRemoval: " + removalNotification.getKey());
                }
            })
            .build();

    private Subscription subscription;
    private BookItemListAdapter bookItemListAdapter;
    private RecyclerView bookList, drawerList;
    private String currentSearchTerm = "cats";
    private Integer currentStartIndex = 0;
    private ProgressBar spinner;
    private Boolean cacheLoading = false;
    private SCROLL_STATUS scrollStatus;

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

        bookList = (RecyclerView)findViewById(R.id.book_item_list_recycler_view);
        bookList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        bookList.setLayoutManager(layoutManager);

        bookList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!cacheLoading) {
                    if (!recyclerView.canScrollVertically(-1)) {
                        scrollStatus = SCROLL_STATUS.SCROLLING_UP;
                        int minKey = Collections.min(bookListItemsCache.asMap().keySet());
                        if(minKey == 0) {
                            return;
                        }
                        spinner.setVisibility(View.VISIBLE);
                        currentStartIndex = (minKey - 1) * MAX_RESULTS;
                        update();
                    } else if (!recyclerView.canScrollVertically(1)) {
                        spinner.setVisibility(View.VISIBLE);
                        scrollStatus = SCROLL_STATUS.SCROLLING_DOWN;
                        int maxKey = Collections.max(bookListItemsCache.asMap().keySet());
                        currentStartIndex = (maxKey + 1) * MAX_RESULTS;
                        update();
                    }
                }
            }
        });

        bookItemListAdapter = new BookItemListAdapter(getApplicationContext(), bookListItemsCache);
        bookList.setAdapter(bookItemListAdapter);

        spinner.setVisibility(View.VISIBLE);
        subscription = Observable
                .defer(() -> Observable.just(getSearchData(currentSearchTerm, currentStartIndex)))
                .flatMap(data -> googleBooksClient.getBooks(data))
                .repeatWhen(repeatHandler ->
                        repeatHandler.flatMap(nothing -> updateSubject.asObservable()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    cacheLoading = false;
                    spinner.setVisibility(View.GONE);
                    if(result.getItems().length == 0) {
                        Toast.makeText(getApplicationContext(), R.string.search_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int cacheKey = currentStartIndex / MAX_RESULTS;
                    bookListItemsCache.put(cacheKey, result);
                    updateBookItemListAdapter();
                }, err -> {
                    Log.i(TAG, err.getMessage());
                    cacheLoading = false;
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), R.string.search_error, Toast.LENGTH_SHORT).show();
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
        cacheLoading = true;
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
        System.out.println("current search term: " + currentSearchTerm);
        System.out.println("current start index: " + currentStartIndex);
        update();
    }

    private void updateBookItemListAdapter() {
        bookItemListAdapter.notifyDataSetChanged();
        if(scrollStatus == SCROLL_STATUS.SCROLLING_UP) {
            bookList.scrollToPosition(MAX_RESULTS + 3);
        } else if(scrollStatus == SCROLL_STATUS.SCROLLING_DOWN) {
            int position = (int)(bookListItemsCache.size() - 1) * MAX_RESULTS - 4 ;
            bookList.scrollToPosition(position);
        }
    }
}
