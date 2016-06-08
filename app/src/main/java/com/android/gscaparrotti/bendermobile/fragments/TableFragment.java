package com.android.gscaparrotti.bendermobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.gscaparrotti.bendermobile.R;
import com.android.gscaparrotti.bendermobile.network.ServerInteractor;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import model.Dish;
import model.IDish;
import model.Order;
import model.Pair;

public class TableFragment extends Fragment {

    private static final String TABLE_NUMBER = "TABLENMBR";
    private int tableNumber;
    private List<Order> list = new LinkedList<>();
    private DishAdapter adapter;
    private Timer timer;
    private String ip;

    private OnTableFragmentInteractionListener mListener;

    public TableFragment() {
    }

    public static TableFragment newInstance(int param1) {
        TableFragment fragment = new TableFragment();
        Bundle args = new Bundle();
        args.putInt(TABLE_NUMBER, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tableNumber = getArguments().getInt(TABLE_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_table, container, false);
        TextView text = (TextView) view.findViewById(R.id.tableTitle);
        text.setText(text.getText() + " " + Integer.toString(tableNumber));
        ListView listView = (ListView) view.findViewById(R.id.dishesList);
        adapter = new DishAdapter(getActivity(), list);
        listView.setAdapter(adapter);
        Button update = (Button) view.findViewById(R.id.updateButton);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAndStartTasks();
            }
        });
        Button addDish = (Button) view.findViewById(R.id.addToTable);
        addDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onAddDishEventFired(tableNumber);
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("FRAGMENT ON RESUME", "FRAGMENT ON RESUME");
        updateAndStartTasks();
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnTableFragmentInteractionListener) {
            mListener = (OnTableFragmentInteractionListener) context;
            ip = getActivity().getSharedPreferences("BenderIP", 0).getString("BenderIP", "Absent");
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMainFragmentInteractionListener");
        }
    }



    public void aggiorna(final List<Order> newList) {
        if (list != null) {
            list.clear();
            list.addAll(newList);
            Collections.sort(list, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    return (o2.getAmounts().getX() - o2.getAmounts().getY()) - (o1.getAmounts().getX() - o1.getAmounts().getY());
                }
            });
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onDestroyView();
        Log.d("FRAGMENT STOP", "FRAGMENT STOP");
        stopTasks();
    }

    private void updateAndStartTasks() {
        new ServerOrdersDownloader().execute(tableNumber);
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new ServerOrdersDownloader().execute(tableNumber);
                }
            }, 0, 3000);
        }
    }

    private void stopTasks() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTableFragmentInteractionListener {
        void onAddDishEventFired(final int tableNumber);
    }

    private class DishAdapter extends ArrayAdapter<Order> {

        private LayoutInflater inflater;

        public DishAdapter(Context context, List<Order> persone) {
            super(context, 0, persone);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_dish, parent, false);
            }
            final Order order = getItem(position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DishDetailFragment detail = DishDetailFragment.newInstance(order);
                    detail.show(getFragmentManager(), "Dialog");
                }
            });
            convertView.setLongClickable(true);
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    order.getAmounts().setY(order.getAmounts().getX());
                    new ServerOrdersUploader().execute(order);
                    return true;
                }
            });
            convertView.findViewById(R.id.removeButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Order toRemove = new Order(order.getTable(), order.getDish(), new Pair<>(-1, 1));
                    new ServerOrdersUploader().execute(toRemove);
                }
            });
            ((TextView) convertView.findViewById(R.id.dish)).setText(order.getDish().getName());
            ((TextView) convertView.findViewById(R.id.dishToServe))
                    .setText(getResources().getString(R.string.StringOrdinati) + Integer.toString(order.getAmounts().getX()));
            ((TextView) convertView.findViewById(R.id.dishServed))
                    .setText(getResources().getString(R.string.StringDaServire) + Integer.toString(order.getAmounts().getX() - order.getAmounts().getY()));
            if (!order.getAmounts().getX().equals(order.getAmounts().getY())) {
                convertView.findViewById(R.id.itemTableLayout).setBackgroundColor(Color.parseColor("#80FF5050"));
            } else {
                convertView.findViewById(R.id.itemTableLayout).setBackgroundColor(Color.parseColor("#8099FF66"));
            }
            return convertView;
        }
    }

    private class ServerOrdersUploader extends AsyncTask<Order, Void, Boolean> {

        private String errorMessage;

        @Override
        protected Boolean doInBackground(Order... params) {
            final ServerInteractor uploader = ServerInteractor.getInstance();
            boolean result = false;
            final Object resultFromServer = uploader.sendCommandAndGetResult(ip, 6789, params[0]);
            if (resultFromServer instanceof Exception) {
                final Exception e = (Exception) resultFromServer;
                errorMessage = e.toString();
            } else if (resultFromServer instanceof String) {
                final String stringResult = (String) resultFromServer;
                if (stringResult.equals("ORDER UPDATED CORRECTLY")) {
                    result = true;
                } else {
                    errorMessage = stringResult;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                new ServerOrdersDownloader().execute(tableNumber);
            } else {
                final List<Order> errors = new LinkedList<>();
                errors.add(new Order(TableFragment.this.tableNumber, new Dish(errorMessage, 0), new Pair<>(0, 1)));
                aggiorna(errors);
            }
        }
    }

    private class ServerOrdersDownloader extends AsyncTask<Integer, Void, List<Order>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Order> doInBackground(Integer... params) {
            //qui effettuerò la chiamata al server
            final List<Order> temp = new LinkedList<>();
            final ServerInteractor dataDownloader = ServerInteractor.getInstance();
            Object input = null;
            if (tableNumber > 0) {
                input = dataDownloader.sendCommandAndGetResult(ip, 6789, "GET TABLE " + params[0]);
            } else if (tableNumber == 0) {
                input = dataDownloader.sendCommandAndGetResult(ip, 6789, "GET PENDING ORDERS");
            } else {
                input = new Exception("Invalid Table Number");
            }
            if (input instanceof Exception) {
                final Exception e = (Exception) input;
                e.printStackTrace();
                Log.e("exception", e.toString());
                temp.add(new Order(TableFragment.this.tableNumber, new Dish(e.toString(), 0), new Pair<>(0, 1)));
                stopTasks();
            } else if (input instanceof Map) {
                //noinspection unchecked
                final Map<IDish, Pair<Integer, Integer>> datas = (Map<IDish, Pair<Integer, Integer>>) input;
                for(final Map.Entry<IDish, Pair<Integer, Integer>> entry : datas.entrySet()) {
                    temp.add(new Order(TableFragment.this.tableNumber, entry.getKey(), entry.getValue()));
                }
            } else if (input instanceof List) {
                //noinspection unchecked
                final List<Order> datas = (List<Order>) input;
                for (final Order o : datas) {
                    temp.add(new Order(o.getTable(), new Dish(o.getDish().getName() + " - " + o.getTable(), o.getDish().getPrice()), o.getAmounts()));
                }
            }
            return temp;
        }

        @Override
        protected void onPostExecute(List<Order> orders) {
            super.onPostExecute(orders);
            TableFragment.this.aggiorna(orders);
        }
    }

}