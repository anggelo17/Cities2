package com.example.luis.cities.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.luis.cities.model.Data;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MHandlerThread<T> extends HandlerThread {

    private boolean mHasQuit=false;
    private static final String TAG ="MHandlerThread";

    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;

    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    public Trie trie;

    private Handler mResponseHandler;
    private IThreadListener<T> mIThreadListener;

    public interface IThreadListener<T>{

        void onTaskCompleted(List<Data> filteredList);

    }

    public void setListener(IThreadListener<T> mIThreadListener){

        this.mIThreadListener= mIThreadListener;

    }


    public MHandlerThread(Handler mResponseHandler){
        super(TAG);
        this.mResponseHandler= mResponseHandler;
    }

    @Override
    public boolean quit() {
        mHasQuit=true;
        return super.quit();

    }


    @Override
    protected void onLooperPrepared() {
        mRequestHandler= new Handler(){

            @Override
            public void handleMessage(Message msg) {
                if(msg.what==MESSAGE_DOWNLOAD){
                    T target = (T) msg.obj;
                    Log.d(TAG, "Got a prefix for: " + mRequestMap.get(target));
                    handle_task(target);
                }
            }
        };

    }

    private void handle_task(final T target){

        final String prefix= mRequestMap.get(target);

        Log.d("filter", "start filtering..with" + prefix);

        long s = System.nanoTime();
        String comp = trie.printSuggestions(trie.rootNode, prefix);
        Log.d("filter", "--" + comp);
        double e = (double) (System.nanoTime() - s) / 1000000000.0;
        Log.d("filter", "finished time==" + e+" size="+trie.lstRes.size());

        final List<Data> filteredList = new ArrayList<>();

        filteredList.addAll(trie.getListResults());

        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mHasQuit) return;
                mRequestMap.remove(target);
                mIThreadListener.onTaskCompleted(filteredList);
            }
        });



    }

    public void queueTask(T target,String prefix){

        mRequestMap.put(target,prefix);
        mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();


    }



}
