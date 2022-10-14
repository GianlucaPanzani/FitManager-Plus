package it.unipi.di.sam.m550358.fitmanager.fragment.Home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import it.unipi.di.sam.m550358.fitmanager.DatabaseHandler;
import it.unipi.di.sam.m550358.fitmanager.R;
import it.unipi.di.sam.m550358.fitmanager.fragment.Pianification.Plan;
import it.unipi.di.sam.m550358.fitmanager.fragment.Settings.SettingsFragment;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private Context context;

    private ArrayList<Workout> workoutList;
    private static WorkoutAdapter thisInstance;


    /**************** Classe ViewHolder ****************/
    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTV, kcalTV, hourStartTV, hourEndTV, dateTV, stepsTV, stepsGoalTV, speedTV;
        ImageView imageView;
        ProgressBar progressBar;

        /** Costruttore della classe**/
        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTV = itemView.findViewById(R.id.work_item_description);
            kcalTV = itemView.findViewById(R.id.work_item_kcal);
            hourEndTV = itemView.findViewById(R.id.work_item_hourEnd);
            hourStartTV = itemView.findViewById(R.id.work_item_hourStart);
            dateTV = itemView.findViewById(R.id.work_item_date);
            stepsTV = itemView.findViewById(R.id.work_item_steps);
            stepsGoalTV = itemView.findViewById(R.id.work_item_steps_goal);
            speedTV = itemView.findViewById(R.id.work_item_speed);
            imageView = itemView.findViewById(R.id.work_item_imageView);
            progressBar = itemView.findViewById(R.id.work_item_progressBar_km);
        }
    }



    /** Costruttore della classe **/
    public WorkoutAdapter(Context context) {
        this.context = context;
    }




    /** Singleton: metodo che assicura la creazione di un'unica istanza di questa classe **/
    public static WorkoutAdapter getInstance(Context context) {
        if (thisInstance == null)
            thisInstance = new WorkoutAdapter(context);
        return thisInstance;
    }




    /** Setta la lista dell'adapter **/
    public void setWorkoutList(ArrayList<Workout> workoutList) {
        if (this.workoutList == null && workoutList != null)
            this.workoutList = workoutList;
    }




    /**
     *  Se l'oggetto e' null e non e' contenuto nella lista, allora viene aggiunto a questa e
     *  sul database (in modo asincrono)
     **/
    public void addItem(Workout w) {
        if (w == null) {
            Log.e("WorkoutAdapter.addItem", "null parameter");
            return;
        }

        if (workoutListContains(w)) {
            Log.e("WorkoutAdapter.addItem", "item already in item's list");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseHandler db = new DatabaseHandler(context);
                if (!db.insertWorkoutRecord(w.date,w.hours,w.description,w.time,w.steps,w.stepsGoal,w.speed,w.imageId))
                    Log.e("WorkoutAdapter.addItem.run", "item not added in database");
            }
        }).start();

        workoutList.add(w);
        thisInstance.notifyItemInserted(workoutList.size()-1);

    }



    /** Restituisce true se l'oggetto workout e' presente nella lista, false altrimenti **/
    private boolean workoutListContains(Workout workout) {
        for (Workout w : workoutList)
            if ((workout.hours+workout.time).equals(w.hours+w.time))
                return true;
        return false;
    }



    @NonNull
    @Override
    public WorkoutAdapter.WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup vg, int viewType) {
        return new WorkoutAdapter.WorkoutViewHolder(
                LayoutInflater.from(vg.getContext())
                        .inflate(R.layout.item_work, vg, false)
        );
    }



    @Override
    public void onBindViewHolder(@NonNull WorkoutAdapter.WorkoutViewHolder workoutVH, int i) {
        DecimalFormat df = new DecimalFormat("0.00");
        Workout workout = workoutList.get(i);

        // calcolo dei valori con cui settare i campi di workoutVH
        int workoutHours = (workout.time/60)/60;
        int workoutMinutes = workout.time/60;
        int hour = Integer.parseInt(workout.hours.substring(0,workout.hours.indexOf(":")));
        int min = Integer.parseInt(workout.hours.substring(workout.hours.indexOf(":")+1));
        double kcal = getKcalByMETsCalculation(workout.imageId,workoutMinutes+(workoutHours*60));

        workoutVH.progressBar.setProgress(Math.min(workout.steps,workout.stepsGoal));
        workoutVH.imageView.setImageResource(workout.imageId);
        workoutVH.dateTV.setText(workout.date);
        workoutVH.hourStartTV.setText("Inizio: " + ((hour<10)?"0":"")+hour + ":" + ((min<10)?"0":"")+min);
        workoutVH.hourEndTV.setText("Fine:   " + getHoursEnd(workout.hours,workoutHours,workoutMinutes));
        workoutVH.descriptionTV.setText(workout.description);
        workoutVH.stepsTV.setText(workout.steps+"");
        workoutVH.stepsGoalTV.setText("/" + workout.stepsGoal);
        workoutVH.speedTV.setText(df.format(workout.speed)+" m/s");
        workoutVH.kcalTV.setText(
                (workoutMinutes == 0)?
                        (
                                "Tempo troppo breve per il calcolo delle kcal "
                        ) : (
                                (kcal == 0)?
                                ("Non hai inserito il tuo peso") :
                                (df.format(kcal) + " kcal")
                        )
        );
        if (!workoutVH.kcalTV.getText().toString().endsWith("kcal"))
            workoutVH.kcalTV.setTextSize(10);
    }




    /** restituisce il valore del MET in base all'id dell'immagine che identifica l'allenamento **/
    public static double getMETs(int imageId) {
        double METs;

        switch (imageId) {
            case R.drawable.ic_walk:
                METs = 5.3; // walking at normal pace
                break;
            case R.drawable.ic_run:
                METs = 7.0; // jogging, general
                break;
            case R.drawable.ic_bike:
                METs = 7.5; // bicycling, general
                break;
            case R.drawable.ic_sports_soccer:
                METs = 7.0; // soccer, general
                break;
            case R.drawable.ic_sports_basketball:
                METs = 6.5; // basketball, general
                break;
            case R.drawable.ic_sports_volleyball:
                METs = 4.0; // volleyball, general
                break;
            case R.drawable.ic_sports_martialarts:
                METs = 10.3; // martial arts, different types, moderate pace
                break;
            case R.drawable.ic_sports_tennis:
                METs = 7.3; // tennis, general
                break;
            case R.drawable.ic_sports_swimming:
                METs = 9.5; // swimming, backstroke, general, training or competition
                break;
            default:
                METs = 7.0;
                Log.e("WorkoutAdapter.getKcalByMETsCalculation", "default case on switch clause");
                break;
        }
        return METs;
    }



    /** Calcola quante Kcal sono state consumate in base al tipo di attivita' e al tempo di allenamento **/
    public double getKcalByMETsCalculation(int imageId, int min) {
        Log.d("GET KCAL BY......",(getMETs(imageId) * 0.0175 * SettingsFragment.KG) * min + " " + "METs="+getMETs(imageId));
        return (getMETs(imageId) * 0.0175 * SettingsFragment.KG) * min;
    }



    @Override
    public int getItemCount() {
        return workoutList.size();
    }



    /**
     * Restituisce l'ora di fine allenamento come: orario_attuale + tempo_allenamento.
     *  Quindi come: hours + (minutesOf(workoutHours) + workoutMinutes)
     **/
    private String getHoursEnd(String hours, int workoutHours, int workoutMinutes) {
        int hour = Integer.parseInt(hours.substring(0,hours.indexOf(":")));
        int min = Integer.parseInt(hours.substring(hours.indexOf(":")+1));

        hour = (hour+workoutHours+((min+workoutMinutes)/60)) % 24;
        min  = (min+workoutMinutes)%60;

        return ((hour<10)?("0"+hour):hour) + ":" + ((min<10)?("0"+min):min);
    }


}
