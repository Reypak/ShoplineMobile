package com.matt.shopline.fragments.analytics;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class Account extends Fragment {
    FirebaseFirestore db;
    private String followersCount;
    private Analytics analytics;
    private MyAnalyticsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_account, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recView);
        GridLayoutManager mLayoutManager = new GridLayoutManager(requireActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        final ArrayList<Analytics> mUploads = new ArrayList<>();
        float[] data = {7, 2, 3, 10, 7, 3, 5, 0};
        analytics = new Analytics("Reach", data, "30", "120");
        mUploads.add(analytics);


        /*String date1 = DateFormat.getDateInstance(DateFormat.DEFAULT).format(new Date());
        DateFormat format = new SimpleDateFormat("d-M-yyyy");
        String dd = format.format(new Date());*/

      /*  Toast.makeText(requireActivity(), String.valueOf(dd), Toast.LENGTH_SHORT).show();
        Date date = null;
        try {
            // set date with time at 00
            date = format.parse(format.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Query reference = db.collection("analytics").document(user.getUid()).collection("followers");
        reference.orderBy("timestamp", Query.Direction.DESCENDING).limit(4).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                float[] data = new float[4];
                int i = 0;
                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                    documentSnapshot.getId();
                    followersCount = documentSnapshot.get("count").toString();
                    data[i] = Float.parseFloat(followersCount);
                    i++;
                    Toast.makeText(requireActivity(), followersCount, Toast.LENGTH_SHORT).show();
                }

                int percent = (int) (data[0] / data[1] * 100);
                int currentCount = (int) data[0];
//                down ▼
                analytics = new Analytics(getString(R.string.followers), data, String.valueOf(currentCount), "▲ " + percent);
                mUploads.add(analytics);
                adapter.notifyDataSetChanged();
            }
        });
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


        adapter = new MyAnalyticsAdapter(requireActivity(), mUploads);
        recyclerView.setAdapter(adapter);

        /*SparkView sparkView = rootView.findViewById(R.id.sparkView);
        float[] data = {0, 20, 30, 13, 70, 67, 15, 55, 0};
        sparkView.setAdapter(new MyAdapter(data));*/

        return rootView;
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
            holder.setTileData(analytics.getTitle(), analytics.getCount(), analytics.getPercentile());
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

        public void setTileData(String title, String count, String percent) {
            TextView titleText = itemView.findViewById(R.id.tvTitle);
            TextView tvCount = itemView.findViewById(R.id.tvCount);
            TextView tvPercent = itemView.findViewById(R.id.tvPercent);

            titleText.setText(title);
            tvCount.setText(count);
            tvPercent.setText(percent + "%");
        }

        public void setGraphData(float[] data) {
//            sparkView.setFillColor(Color.RED);
            if (data != null) {
                sparkView.setAdapter(new MyAdapter(data));
            }
        }
    }

    public static class MyAdapter extends SparkAdapter {
        private final float[] yData;

        public MyAdapter(float[] yData) {
            this.yData = yData;
        }

        @Override
        public int getCount() {
            return yData.length;
        }

        @NonNull
        @Override
        public Object getItem(int index) {
            return yData[index];
        }

        @Override
        public float getY(int index) {
            return yData[index];
        }
    }
}