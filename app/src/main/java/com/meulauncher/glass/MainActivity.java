package com.meulauncher.glass;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private final List<ResolveInfo> apps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        carregarApps();

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.rgb(30, 28, 32));
        raiz.setPadding(dp(48), dp(32), dp(48), dp(24));

        TextView titulo = new TextView(this);
        titulo.setText("Meu Launcher");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(28);
        raiz.addView(titulo);

        GridView grade = new GridView(this);
        grade.setNumColumns(6);
        grade.setVerticalSpacing(dp(16));
        grade.setHorizontalSpacing(dp(16));
        grade.setPadding(0, dp(24), 0, 0);
        grade.setClipToPadding(false);

        GradientDrawable destaque = new GradientDrawable();
        destaque.setColor(Color.argb(70, 255, 255, 255));
        destaque.setStroke(dp(2), Color.WHITE);
        destaque.setCornerRadius(dp(16));
        grade.setSelector(destaque);

        grade.setAdapter(new AppsAdapter());
        grade.setOnItemClickListener((pai, v, posicao, id) -> abrir(apps.get(posicao)));
        raiz.addView(grade, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(raiz);
        grade.requestFocus();
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    private void carregarApps() {
        final PackageManager pm = getPackageManager();
        Set<String> vistos = new HashSet<>();
        String[] categorias = {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER};
        for (String categoria : categorias) {
            Intent filtro = new Intent(Intent.ACTION_MAIN).addCategory(categoria);
            for (ResolveInfo info : pm.queryIntentActivities(filtro, 0)) {
                if (vistos.add(info.activityInfo.packageName)) {
                    apps.add(info);
                }
            }
        }
        Collections.sort(apps, (a, b) ->
                a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));
    }

    private void abrir(ResolveInfo info) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName(
                    info.activityInfo.packageName, info.activityInfo.name));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir este app", Toast.LENGTH_SHORT).show();
        }
    }

    private class AppsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return apps.size();
        }

        @Override
        public Object getItem(int i) {
            return apps.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            LinearLayout item;
            ImageView icone;
            TextView nome;
            if (convertView == null) {
                item = new LinearLayout(MainActivity.this);
                item.setOrientation(LinearLayout.VERTICAL);
                item.setGravity(Gravity.CENTER);
                item.setPadding(dp(8), dp(16), dp(8), dp(12));

                icone = new ImageView(MainActivity.this);
                icone.setId(1);
                item.addView(icone, new LinearLayout.LayoutParams(dp(72), dp(72)));

                nome = new TextView(MainActivity.this);
                nome.setId(2);
                nome.setTextColor(Color.WHITE);
                nome.setTextSize(14);
                nome.setGravity(Gravity.CENTER);
                nome.setMaxLines(1);
                nome.setEllipsize(TextUtils.TruncateAt.END);
                item.addView(nome);
            } else {
                item = (LinearLayout) convertView;
                icone = item.findViewById(1);
                nome = item.findViewById(2);
            }
            PackageManager pm = getPackageManager();
            ResolveInfo info = apps.get(i);
            icone.setImageDrawable(info.loadIcon(pm));
            nome.setText(info.loadLabel(pm));
            return item;
        }
    }
    }
