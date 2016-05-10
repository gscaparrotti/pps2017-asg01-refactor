package com.android.gscaparrotti.bendermobile;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends Activity implements TableFragment.OnTableFragmentInteractionListener, MainFragment.OnMainFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceFragment(MainFragment.newInstance(), false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("STOP", "STOP");
        final ServerInteractor interactor = ServerInteractor.getInstance();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        interactor.sendCommandAndGetResult("10.0.2.2", 6789, "CLOSE CONNECTION");
                        interactor.interactionEnded();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else
            super.onBackPressed();

    }

    private void replaceFragment(Fragment fragment, boolean back) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        if (back)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onTablePressed(int tableNumber) {
        replaceFragment(TableFragment.newInstance(tableNumber), true);
    }


    @Override
    public void onLoadingInProgress() {

    }
}
