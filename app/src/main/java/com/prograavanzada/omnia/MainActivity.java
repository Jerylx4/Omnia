package com.prograavanzada.omnia;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prograavanzada.omnia.ui.accounts.AddAccountBottomSheet;
import com.prograavanzada.omnia.ui.auth.LoginActivity;
import com.prograavanzada.omnia.ui.dashboard.DashboardFragment;
import com.prograavanzada.omnia.ui.transactions.AddTransactionBottomSheet;
import com.prograavanzada.omnia.ui.transactions.TransactionsFragment;
import com.prograavanzada.omnia.update.UpdateOrchestrator;

public class MainActivity extends AppCompatActivity {

    private boolean isFabExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_transactions) {
                selectedFragment = new TransactionsFragment();
            } else {
                selectedFragment = new DashboardFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }
            return true;
        });

        FloatingActionButton fabMain = findViewById(R.id.fabAdd);
        FloatingActionButton fabAccount = findViewById(R.id.fabAddAccount);
        FloatingActionButton fabTransaction = findViewById(R.id.fabAddTransaction);
        TextView tvAccount = findViewById(R.id.tvFabAccount);
        TextView tvTransaction = findViewById(R.id.tvFabTransaction);

        fabMain.setOnClickListener(v -> {
            isFabExpanded = !isFabExpanded;
            if (isFabExpanded) {
                fabMain.animate().rotation(45f).setDuration(200).start();
                fabAccount.setVisibility(View.VISIBLE);
                fabTransaction.setVisibility(View.VISIBLE);
                tvAccount.setVisibility(View.VISIBLE);
                tvTransaction.setVisibility(View.VISIBLE);
            } else {
                fabMain.animate().rotation(0f).setDuration(200).start();
                fabAccount.setVisibility(View.GONE);
                fabTransaction.setVisibility(View.GONE);
                tvAccount.setVisibility(View.GONE);
                tvTransaction.setVisibility(View.GONE);
            }
        });

        fabAccount.setOnClickListener(v -> {
            fabMain.performClick();
            AddAccountBottomSheet bottomSheetAccount = new AddAccountBottomSheet();
            bottomSheetAccount.show(getSupportFragmentManager(), "AddAccountBottomSheet");
        });

        fabTransaction.setOnClickListener(v -> {
            fabMain.performClick();
            AddTransactionBottomSheet bottomSheetTransaction = new AddTransactionBottomSheet();
            bottomSheetTransaction.show(getSupportFragmentManager(), "AddTransactionBottomSheet");
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new DashboardFragment())
                    .commit();
        }

        try {
            String currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            UpdateOrchestrator updater = new UpdateOrchestrator(
                    this,
                    "Jerylx4",
                    "Omnia",
                    currentVersion
            );
            updater.executeUpdateFlow();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}