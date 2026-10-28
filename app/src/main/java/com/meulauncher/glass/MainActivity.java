package com.meulauncher.glass;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
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

        FrameLayout raiz = new FrameLayout(this);
        raiz.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF1B1A1E, 0xFF5A433E, 0xFF2A2325, 0xFF161417}));

        LinearLayout coluna = new LinearLayout(this);
        coluna.setOrientation(LinearLayout.VERTICAL);
        coluna.setPadding(dp(48), dp(28), dp(48), dp(32));
        coluna.addView(criarCabecalho());

        LinearLayout painel = new LinearLayout(this);
        painel.setOrientation(LinearLayout.VERTICAL);
        painel.setBackground(criarVidro(28, 40, 8));
        painel.setPadding(dp(28), dp(24), dp(28), dp(8));

        TextView secao = new TextView(this);
        secao.setText("Todos os aplicativos");
        secao.setTextColor(Color.WHITE);
        secao.setTextSize(20);
        painel.addView(secao);

        GridView grade = new GridView(this);
        grade.setNumColumns(6);
        grade.setVerticalSpacing(dp(12));
        grade.setHorizontalSpacing(dp(12));
        grade.setPadding(0, dp(16), 0, 0);
        grade.setClipToPadding(false);

        GradientDrawable destaque = new GradientDrawable();
        destaque.setColor(Color.argb(45, 255, 255, 255));
        destaque.setStroke(dp(3), Color.WHITE);
        destaque.setCornerRadius(dp(18));
        grade.setSelector(destaque);
        grade.setDrawSelectorOnTop(true);

        grade.setAdapter(new AppsAdapter());
        grade.setOnItemClickListener((pai, v, posicao, id) -> abrir(apps.get(posicao)));
        painel.addView(grade, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout.LayoutParams parametrosPainel = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        parametrosPainel.topMargin = dp(24);
        coluna.addView(painel, parametrosPainel);

        raiz.addView(coluna, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(raiz);
        grade.requestFocus();
    }

    private View criarCabecalho() {
        LinearLayout cabecalho = new LinearLayout(this);
        cabecalho.setOrientation(LinearLayout.HORIZONTAL);
        cabecalho.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = new TextView(this);
        titulo.setText("Meu Launcher");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(26);
        cabecalho.addView(titulo, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout relogio = new LinearLayout(this);
        relogio.setOrientation(LinearLayout.VERTICAL);
        relogio.setGravity(Gravity.END);

        TextClock hora = new TextClock(this);
        hora.setFormat24Hour("HH:mm");
        hora.setFormat12Hour("HH:mm");
        hora.setTextColor(Color.WHITE);
        hora.setTextSize(30);
        relogio.addView(hora);

        TextClock data = new TextClock(this);
        data.setFormat24Hour("EEE, d MMM");
        data.setFormat12Hour("EEE, d MMM");
        data.setTextColor(Color.argb(200, 255, 255, 255));
        data.setTextSize(14);
        relogio.addView(data);

        cabecalho.addView(relogio);
        return cabecalho;
    }

    private Drawable criarVidro(int raio, int alfaTopo, int alfaBase) {
        GradientDrawable escuro = new GradientDrawable();
        escuro.setColor(Color.argb(40, 24, 22, 26));
        escuro.setCornerRadius(dp(raio));

        GradientDrawable brilho = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.argb(alfaTopo, 255, 255, 255),
                        Color.argb(alfaBase, 255, 255, 255)});
        brilho.setCornerRadius(dp(raio));
        brilho.setStroke(dp(1), Color.argb(90, 255, 255, 255));

        return new LayerDrawable(new Drawable[]{escuro, brilho});
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
                item.setPadding(dp(8), dp(14), dp(8), dp(12));

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
