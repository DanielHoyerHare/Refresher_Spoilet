package big.dick.refresher;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest.*;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import big.dick.refresher.models.food;

public class StartupActivity extends AppCompatActivity {

    public static List<food> foodList;

    FusedLocationProviderClient flpc;

    ArrayList<String> permissionsList;
    String[] permissionsStr = {
            permission.ACCESS_FINE_LOCATION
    };
    int permissionsCount = 0;

    TextView txt_lat, txt_lon, txt_alt, txt_dir;
    private ActivityResultLauncher<String[]> permissionsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.startup);

        Log.d("Main", "OnCreate success");

        flpc = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        EditText input = findViewById(R.id.edit);
        Button submit = findViewById(R.id.submit);

        foodList = new ArrayList<>();

        submit.setOnClickListener(view ->
            foodList.add(new food(input.getText().toString()))
        );

        permissionsLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        new ActivityResultCallback<Map<String, Boolean>>() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onActivityResult(Map<String,Boolean> result) {
                                ArrayList<Boolean> list = new ArrayList<>(result.values());
                                permissionsList = new ArrayList<>();
                                permissionsCount = 0;
                                for (int i = 0; i < list.size(); i++) {
                                    if (shouldShowRequestPermissionRationale(permissionsStr[i])) {
                                        permissionsList.add(permissionsStr[i]);
                                    }else if (!hasPermission(StartupActivity.this, permissionsStr[i])){
                                        permissionsCount++;
                                    }
                                }
                                if (permissionsList.size() > 0) {
                                    //Some permissions are denied and can be asked again.
                                    askForPermissions(permissionsList);
                                } else if (permissionsCount > 0) {
                                    //Show alert dialog
                                    showPermissionDialog();
                                } else {
                                    //All permissions granted. Do your stuff 🤞
                                }
                            }
                        });

        permissionsList = new ArrayList<>();
        permissionsList.addAll(Arrays.asList(permissionsStr));
        askForPermissions(permissionsList);

        txt_lat = findViewById(R.id.txt_lat);
        txt_lon = findViewById(R.id.txt_lon);
        txt_alt = findViewById(R.id.txt_alt);
        txt_dir = findViewById(R.id.txt_dir);
    }
    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);
        } else {
        /* User has pressed 'Deny & Don't ask again' so we have to show the enable permissions dialog
        which will lead them to app details page to enable permissions from there. */
            showPermissionDialog();
        }
    }
    AlertDialog alertDialog;
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setMessage("Some permissions are need to be allowed to use this app without any problems.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    dialog.dismiss();
                });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }
}