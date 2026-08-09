package com.prograavanzada.omnia;

import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.prograavanzada.omnia.update.UpdateOrchestador;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            String currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

            UpdateOrchestador updater = new UpdateOrchestador(
                    this,
                    "Jerylx4",
                    "TU_REPOSITORIO",
                    currentVersion
            );
            updater.executedUpdateFlow();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}