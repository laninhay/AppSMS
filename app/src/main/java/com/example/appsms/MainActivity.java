package com.example.appsms;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etFilter;
    private TextView tvCurrentFilter;
    private SmsAdapter adapter;
    private SharedPreferences prefs;
    private TextView tvEmptyState;

    private final SharedPreferences.OnSharedPreferenceChangeListener prefsListener =
            (sharedPreferences, key) -> {
                if ("saved_messages".equals(key)) {
                    carregarMensagens();
                }
            };

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

        etFilter = findViewById(R.id.etFilter);
        Button btnSaveFilter = findViewById(R.id.btnSaveFilter);
        tvCurrentFilter = findViewById(R.id.tvCurrentFilter);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SmsAdapter();
        recyclerView.setAdapter(adapter);

        prefs = getSharedPreferences("AppSMSPrefs", Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        atualizarTextoFiltro();
        carregarMensagens();

        btnSaveFilter.setOnClickListener(v -> {
            String novoFiltro = etFilter.getText().toString().trim();
            if (!novoFiltro.isEmpty()) {
                prefs.edit().putString("filter_word", novoFiltro).apply();
                atualizarTextoFiltro();
                etFilter.setText("");
                Toast.makeText(this, "Filtro atualizado!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Digite uma palavra para o filtro", Toast.LENGTH_SHORT).show();
            }
        });

        checarPermissoes();
    }

    private void atualizarTextoFiltro() {
        String filtro = prefs.getString("filter_word", "");
        if (filtro.isEmpty()) {
            tvCurrentFilter.setText("Filtro atual: Nenhum (SMS não serão salvos)");
        } else {
            tvCurrentFilter.setText("Filtro atual: " + filtro);
        }
    }

    private void carregarMensagens() {
        String jsonString = prefs.getString("saved_messages", "[]");
        List<SmsModel> lista = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                lista.add(new SmsModel(obj.getString("sender"), obj.getString("body")));
            }

            List<SmsModel> listaInvertida = new ArrayList<>();
            for (int i = lista.size() - 1; i >= 0; i--) {
                listaInvertida.add(lista.get(i));
            }

            adapter.setSmsList(listaInvertida);

            if (lista.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                findViewById(R.id.recyclerView).setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void checarPermissoes() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "O App não pode funcionar sem a permissão", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (prefs != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
        }
    }
}