package com.example.luis.cities.ui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.luis.cities.CityAdapter;
import com.example.luis.cities.R;
import com.example.luis.cities.model.Coord;
import com.example.luis.cities.model.Data;
import com.example.luis.cities.util.MHandlerThread;
import com.example.luis.cities.util.MyLoader;

import java.util.ArrayList;
import java.util.List;

public class FragmentOne extends BaseFragment implements CityAdapter.CityListener,LoaderManager.LoaderCallbacks<List<Data>>{

    private RecyclerView recyclerView;
    private List<Data> citiesList;
    private CityAdapter adapter;
    private SearchView searchView;
    private View loadingBar;

    private IlistenerFrag ilistenerFrag;

    public MHandlerThread<Integer> mHandlerThread;

    public interface IlistenerFrag{

        void onSelected(Data data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            ilistenerFrag = (IlistenerFrag) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Handler response=new Handler();
        mHandlerThread = new MHandlerThread<>(response);
        mHandlerThread.setListener(new MHandlerThread.IThreadListener<Integer>() {
            @Override
            public void onTaskCompleted(List<Data> filteredList) {
                adapter.updateData(filteredList);
            }
        });


        mHandlerThread.start();
        mHandlerThread.getLooper();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandlerThread.quit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_scrolling,menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        setSearchView((SearchView) menu.findItem(R.id.action_search)
                .getActionView());
        getSearchView().setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        getSearchView().setMaxWidth(Integer.MAX_VALUE);

        getSearchView().setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("frag","submit...");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.filter(query);
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initViews(View view) {


        recyclerView = view.findViewById(R.id.recycler_view);
        loadingBar = view.findViewById(R.id.loading);
        citiesList=new ArrayList<>();
        adapter= new CityAdapter(getContext(),citiesList,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(adapter);

        //initData();

        Log.d("frag","init Loader again...");

        getActivity().getSupportLoaderManager().initLoader(R.id.string_id,null, this);



    }


    @NonNull
    @Override
    public Loader<List<Data>> onCreateLoader(int id, @Nullable Bundle args) {
        return new MyLoader(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Data>> loader, List<Data> data) {

        Log.d("lst","---"+data.get(0).getName()+data.get(0).getCountry());

        adapter.initialLoad(data);
        MyLoader myLoader= (MyLoader) loader;
        adapter.setTrie(myLoader.trie);
        adapter.setMHandler(mHandlerThread);

        loadingBar.setVisibility(View.GONE);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Data>> loader) {

    }

    @Override
    public void onCitySelected(Data city) {

        ilistenerFrag.onSelected(city);


    }

    public SearchView getSearchView() {
        return searchView;
    }

    public void setSearchView(SearchView searchView) {
        this.searchView = searchView;
    }
}
