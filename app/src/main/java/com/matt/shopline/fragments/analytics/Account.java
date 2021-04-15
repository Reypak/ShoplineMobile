package com.matt.shopline.fragments.analytics;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.matt.shopline.R;
import com.matt.shopline.objects.Analytics;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Account extends Fragment {
    FirebaseFirestore db;
    private Analytics analytics;
    private MyAnalyticsAdapter adapter;
    private ArrayList<Analytics> mUploads;
    private FirebaseUser user;
    private String date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_account, container, false);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        DateFormat format = new SimpleDateFormat("d-M-yyyy");
        date = format.format(new Date());

        Bundle bundle = getArguments();
        if (bundle != null) {
            // get date string
            date = bundle.getString("date");
            try {
                Date date1 = DateFormat.getDateInstance(DateFormat.DEFAULT).parse(date);
                // format short date to Date main format
                date = format.format(date1);
            } catch (ParseException ignored) {

            }
        }

        RecyclerView recyclerView = rootView.findViewById(R.id.recView);
        GridLayoutManager mLayoutManager = new GridLayoutManager(requireActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        mUploads = new ArrayList<>();

        addData(getString(R.string.followers));
        addData("Likes");
        addData(getString(R.string.comments));
        addData("Reposts");

        adapter = new MyAnalyticsAdapter(requireActivity(), mUploads);
        recyclerView.setAdapter(adapter);

        return rootView;

        /*String date1 = DateFormat.getDateInstance(DateFormat.DEFAULT).format(new Date());
         */

      /*  Toast.makeText(requireActivity(), String.valueOf(dd), Toast.LENGTH_SHORT).show();
        Date date = null;
        try {
            // set date with time at 00
            date = format.parse(format.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }*/



       /* reference.document(dd).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()){
                    followersCount = task.getResult().get("count").toString();
                    Toast.makeText(requireActivity(), task.getResult().get("count").toString(), Toast.LENGTH_SHORT).show();
                    analytics = new Analytics(getString(R.string.followers), new float[]{10, Float.parseFloat(followersCount)}, followersCount, "10%");
                    mUploads.add(analytics);
                    adapter.notifyDataSetChanged();
                }
            }
        });*/

        /*reference.whereGreaterThan("timestamp", date).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                            documentSnapshot.getId();
                            Toast.makeText(requireActivity(), documentSnapshot.get("count").toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/

   /*     Map<String, Object> map = new HashMap<>();
        map.put("timestamp", date);
        map.put("count", "30");
        reference.document(String.valueOf(new Date())).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(requireActivity(), "Done", Toast.LENGTH_SHORT).show();
            }
        });
        */

    }

    private void addData(final String path) {
        Query reference = db.collection("analytics")
                .document(user.getUid())
                .collection(path.toLowerCase());
        reference.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(4).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // collect for graph values
                            List<Float> floats = new ArrayList<>();
                            // collect dates for spinner
                            ArrayList<String> dates = new ArrayList<>();

                            int i = 1; // start at 1
                            floats.add(0, (float) 0); // set default to 0

                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                // get count value
                                String count = documentSnapshot.get("count").toString();
                                dates.add(documentSnapshot.getId()); // get ID
                                // current Date
                                if (documentSnapshot.getId().equals(date)) {
                                    // set default value
                                    floats.set(0, Float.valueOf(count));
                                } else {
                                    // continue
                                    floats.add(i, Float.valueOf(count));
                                    i++;
                                }
                            }

                            if (floats.size() <= 1) {
                                // add second value
                                floats.add((float) 0);
                            }
                            String currentCount = String.valueOf(floats.get(0).intValue());
                            // calculate percent
                            int percent = 0;
                            String inc = "▲ ";

                            if (floats.size() >= 1) {
                                if (floats.get(1).intValue() != 0) {
                                    float value = (floats.get(0) / floats.get(1));
                                    if (value < 1) {
                                        inc = "▼ ";
                                    }
                                    percent = (int) (value * 100);
                                }
                            }

                            // send broadcast
                            Intent intent = new Intent("finish");
                            intent.putStringArrayListExtra("dates", dates);
                            requireActivity().sendBroadcast(intent);

                            analytics = new Analytics(path, floats, currentCount, inc + percent);
                            mUploads.add(analytics);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private static class MyAnalyticsAdapter extends RecyclerView.Adapter<AnalyticsViewHolder> {
        private final Context mContext;
        private final List<Analytics> mUploads;

        public MyAnalyticsAdapter(Context mContext, List<Analytics> mUploads) {
            this.mContext = mContext;
            this.mUploads = mUploads;
        }

        @NonNull
        @Override
        public AnalyticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.analytics_item, parent, false);
            return new AnalyticsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AnalyticsViewHolder holder, int position) {
            Analytics analytics = mUploads.get(position);
            holder.setTileData(mContext, analytics.getTitle(), analytics.getCount(), analytics.getPercentile());
            holder.setGraphData(analytics.getData());
        }

        @Override
        public int getItemCount() {
            return mUploads.size();
        }
    }

    public static class AnalyticsViewHolder extends RecyclerView.ViewHolder {
        private final SparkView sparkView = itemView.findViewById(R.id.sparkView);

        public AnalyticsViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setTileData(Context context, String title, String count, String percent) {
            TextView titleText = itemView.findViewById(R.id.tvTitle);
            TextView tvCount = itemView.findViewById(R.id.tvCount);
            TextView tvPercent = itemView.findViewById(R.id.tvPercent);

            titleText.setText(title);
            tvCount.setText(count);
            tvPercent.setText(String.format("%s%%", percent));

            if (percent.contains("▼")) {
                tvPercent.setTextColor(Color.RED);
            }
            if (title.contains("Reposts")) {
                setColors(context, titleText, R.color.colorHighlight, "#4DFF8C39");
            } else if (title.contains("Likes")) {
                setColors(context, titleText, android.R.color.holo_red_light, "#4DFF4444");
            } else if (title.contains(context.getString(R.string.comments))) {
                setColors(context, titleText, R.color.colorPrimary, "#5E5458F7");
            }
        }

        private void setColors(Context context, TextView textView, int res, String colorString) {
            int orange = context.getResources().getColor(res);
            textView.setTextColor(orange);
            sparkView.setFillColor(Color.parseColor(colorString));
            sparkView.setLineColor(orange);
        }

        public void setGraphData(List<Float> data) {
            if (data != null) {
                sparkView.setAdapter(new MyAdapter(data));
            }
        }
    }

    public static class MyAdapter extends SparkAdapter {
        private final List<Float> yData;

        public MyAdapter(List<Float> yData) {
            this.yData = yData;
        }

        @Override
        public int getCount() {
            return yData.size();
        }

        @NonNull
        @Override
        public Object getItem(int index) {
            return yData.get(index);
        }

        @Override
        public float getY(int index) {
            return yData.get(index);
        }
    }
}