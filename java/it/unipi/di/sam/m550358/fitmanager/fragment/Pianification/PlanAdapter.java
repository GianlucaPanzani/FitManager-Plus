package it.unipi.di.sam.m550358.fitmanager.fragment.Pianification;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import it.unipi.di.sam.m550358.fitmanager.DatabaseHandler;
import it.unipi.di.sam.m550358.fitmanager.MainActivity;
import it.unipi.di.sam.m550358.fitmanager.R;
import it.unipi.di.sam.m550358.fitmanager.fragment.Music.MusicPlayerActivity;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {
    private Context context;

    private ArrayList<Plan> planList;
    private static PlanAdapter thisInstance;


    /**************** Classe ViewHolder ****************/
    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        Context context;
        TextView textDate;
        TextView textHours;
        TextView textWork;
        CheckBox checkBox;
        int i;

        /** Costruttore della classe **/
        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            textDate  = itemView.findViewById(R.id.pianification_date);
            textHours = itemView.findViewById(R.id.pianification_hours);
            textWork  = itemView.findViewById(R.id.pianification_work);
            checkBox  = itemView.findViewById(R.id.pianification_checkBox);
        }


        /** Setta il colore di background del campo date in base al valore del checkbox **/
        public void changeDateBackground() {
            Date date = new Date();
            String[] planDate = textDate.getText().toString().split(" ");
            String[] planHours = textHours.getText().toString().split(":");

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(planDate[0]));
            calendar.set(Calendar.MONTH, fromStringToMonth(planDate[1]));
            calendar.set(Calendar.YEAR, Integer.parseInt(planDate[2]));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(planHours[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(planHours[1]));

            // cambia il colore
            if (checkBox.isChecked())
                textDate.setBackgroundColor(context.getColor(R.color.green));
            else if (!date.before(calendar.getTime()))
                textDate.setBackgroundColor(context.getColor(R.color.secondary_text));
            else
                textDate.setBackgroundColor(context.getColor(R.color.accent));
        }


        /** Converte il mese sotto forma di stringa nel mese sotto forma di intero **/
        private int fromStringToMonth(String month) {
            switch (month) {
                case "Gen":  return 0;
                case "Feb":  return 1;
                case "Mar":  return 2;
                case "Apr":  return 3;
                case "Mag":  return 4;
                case "Giu":  return 5;
                case "Lug":  return 6;
                case "Ago":  return 7;
                case "Set":  return 8;
                case "Ott":  return 9;
                case "Nov":  return 10;
                case "Dic":  return 11;
                default: return -1;
            }
        }
    }



    /** Costruttore della classe **/
    public PlanAdapter(Context context) {
        if (this.context == null)
            this.context = context;
    }



    /** Singleton: metodo che assicura la creazione di un'unica istanza di questa classe **/
    public static PlanAdapter getInstance(Context context) {
        if (thisInstance == null)
            thisInstance = new PlanAdapter(context);
        return thisInstance;
    }



    /** Setta la lista dell'adapter **/
    public void setPlanList(ArrayList<Plan> planList) {
        if (this.planList == null && planList != null)
            this.planList = planList;
    }




    /**
     *  Aggiunge il piano di allenamento passato come parametro alla lista e al
     *  database (a quest'ultimo in modo asincrono)
     **/
    public void addItem(Plan plan) {
        for (Plan p : planList)
            // caso di piano di allenamento gia' presente il lista
            if (p.getDate().equals(plan.getDate()) && p.getTextHours().equals(plan.getTextHours())) {
                Toast.makeText(context, "Hai un altro allenamento alla stessa ora!", Toast.LENGTH_SHORT).show();
                return;
            }

        // aggiunta del piano al database
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseHandler db = new DatabaseHandler(context);
                if (!db.insertPlanRecord(plan.date, plan.textHours, plan.textWork, 0))
                    Log.e("PlanAdapter.addItem", "don't written on database");
            }
        }).start();

        // aggiunta del piano alla lista
        planList.add(plan);
        this.notifyItemInserted(planList.size()-1);
    }



    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlanAdapter.PlanViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_pianification, parent, false)
        );
    }



    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder planVH, int i) {
        Plan plan = planList.get(i);

        planVH.i = i;
        planVH.textDate.setText(plan.date);
        planVH.textHours.setText(plan.textHours);
        planVH.textWork.setText(plan.textWork);
        planVH.checkBox.setChecked(plan.checked);
        planVH.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.pianification_checkBox) {
                    // rimozione dal database
                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseHandler db = new DatabaseHandler(context);
                            if (!db.deletePlanRecord(plan.date, plan.textHours))
                                Log.e("PlanAdapter.onBindVH.onClick", "don't updated on database");
                        }
                    }).start();

                    // rimozione dalla lista
                    planList.remove(i);
                    thisInstance.notifyItemRemoved(i);*/
                    planVH.changeDateBackground();
                    planList.get(i).checked = planVH.checkBox.isChecked();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseHandler db = new DatabaseHandler(context);
                            if (!db.updatePlanRecord(plan.date, plan.textHours, plan.textWork, planVH.checkBox.isChecked()))
                                Log.e("PlanAdapter.onBindVH.onClick", "don't updated on database");
                        }
                    }).start();
                    return;
                }

                Log.e("PlanAdapter.onBindVH.onClick", "no matches");
            }
        });

        // setta i long click listener
        planVH.textDate.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return removeItemMethod(view,plan,i);
            }
        });
        planVH.textHours.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return removeItemMethod(view,plan,i);
            }
        });
        planVH.textWork.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return removeItemMethod(view,plan,i);
            }
        });
        planVH.checkBox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return removeItemMethod(view,plan,i);
            }
        });
        planVH.changeDateBackground();
    }



    @Override
    public int getItemCount() {
        return planList.size();
    }




    /** Fa comparire un popup menu a video ed implementa il comportamento in conseguenza del click **/
    private boolean removeItemMethod(View view, Plan plan, int i) {
        // creazione del menu
        PopupMenu planMenu = new PopupMenu(context, view);
        planMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // caso di rimozione del plan dalla lista e dal database (in modo asincrono)
                if (menuItem.getItemId() == R.id.remove_item) {
                    // rimozione da database
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseHandler db = new DatabaseHandler(context);
                            if (!db.deletePlanRecord(plan.date, plan.textHours))
                                Log.e("PlanAdapter.onMenuItemClick", "plan not deleted from database");
                        }
                    }).start();

                    // rimozione da lista
                    planList.remove(i);
                    thisInstance.notifyItemRemoved(i);
                    return true;
                }
                return false;
            }

        });
        planMenu.inflate(R.menu.remove_menu);
        planMenu.show();
        return false;
    }
}
