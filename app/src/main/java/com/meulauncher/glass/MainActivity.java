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
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
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

    private static final int TODO = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int AUTO = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int BRANCO = Color.WHITE;

    private final List<ResolveInfo> apps = new ArrayList<>();
    private View primeiroFoco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        carregarApps();

        FrameLayout raiz = new FrameLayout(this);
        raiz.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF1B1A1E, 0xFF5A433E, 0xFF2A2325, 0xFF161417}));

        LinearLayout coluna = vertical();
        coluna.setPadding(dp(28), dp(16), dp(28), dp(16));
        coluna.addView(criarTopo(), lp(TODO, AUTO));

        LinearLayout corpo = horizontal();
        corpo.addView(criarLateral(), lp(dp(110), TODO));
        LinearLayout.LayoutParams paramConteudo = lp(0, TODO, 1f);
        paramConteudo.leftMargin = dp(12);
        corpo.addView(criarConteudo(), paramConteudo);

        LinearLayout.LayoutParams paramCorpo = lp(TODO, 0, 1f);
        paramCorpo.topMargin = dp(10);
        coluna.addView(corpo, paramCorpo);

        raiz.addView(coluna, new FrameLayout.LayoutParams(TODO, TODO));
        setContentView(raiz);
        if (primeiroFoco != null) {
            primeiroFoco.requestFocus();
        }
    }

    private View criarTopo() {
        LinearLayout topo = horizontal();
        topo.setGravity(Gravity.CENTER_VERTICAL);

        topo.addView(texto("Meu Launcher", 20, BRANCO), lp(0, AUTO, 1f));

        LinearLayout menu = horizontal();
        menu.setGravity(Gravity.CENTER_VERTICAL);
        menu.setBackground(criarVidro(22));
        menu.setPadding(dp(8), dp(4), dp(8), dp(4));
        String[] itens = {"Início", "Aplicativos", "Filmes e Séries", "Música", "Configurações"};
        for (int i = 0; i < itens.length; i++) {
            final String nome = itens[i];
            TextView item = texto(nome, 13, BRANCO);
            item.setPadding(dp(14), dp(7), dp(14), dp(7));
            item.setFocusable(true);
            item.setBackground(fundoFoco(16, cor(i == 0 ? 45 : 0, 16)));
            item.setOnClickListener(v -> avisar(nome));
            menu.addView(item);
        }
        topo.addView(menu, lp(AUTO, AUTO));

        LinearLayout relogio = vertical();
        relogio.setGravity(Gravity.END);
        TextClock hora = new TextClock(this);
        hora.setFormat24Hour("HH:mm");
        hora.setFormat12Hour("HH:mm");
        hora.setTextColor(BRANCO);
        hora.setTextSize(24);
        relogio.addView(hora);
        TextClock data = new TextClock(this);
        data.setFormat24Hour("EEE, d MMM");
        data.setFormat12Hour("EEE, d MMM");
        data.setTextColor(Color.argb(200, 255, 255, 255));
        data.setTextSize(12);
        relogio.addView(data);
        topo.addView(relogio, lp(0, AUTO, 1f));
        return topo;
    }

    private View criarLateral() {
        LinearLayout lateral = vertical();
        lateral.setBackground(criarVidro(22));
        lateral.setPadding(dp(8), dp(10), dp(8), dp(10));
        String[] itens = {"Início", "Meus Apps", "Favoritos", "Pesquisar", "Configurações"};
        for (int i = 0; i < itens.length; i++) {
            final String nome = itens[i];
            TextView item = texto(nome, 13, BRANCO);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(12), 0, dp(8), 0);
            item.setFocusable(true);
            item.setBackground(fundoFoco(14, cor(i == 0 ? 45 : 0, 14)));
            item.setOnClickListener(v -> avisar(nome));
            LinearLayout.LayoutParams p = lp(TODO, dp(40));
            p.bottomMargin = dp(6);
            lateral.addView(item, p);
        }
        return lateral;
    }

    private View criarConteudo() {
        LinearLayout conteudo = vertical();

        LinearLayout linha1 = horizontal();

        LinearLayout banner = vertical();
        banner.setBackground(criarVidro(22));
        banner.setPadding(dp(22), dp(16), dp(22), dp(16));
        banner.addView(texto("SUGESTÕES DE FILMES E SÉRIES", 10, Color.argb(200, 255, 255, 255)));
        TextView chamada = texto("Em breve: sugestões do dia", 24, BRANCO);
        chamada.setPadding(0, dp(8), 0, dp(8));
        banner.addView(chamada);
        banner.addView(texto("Aqui vão aparecer filmes, séries e vídeos do YouTube.",
                13, Color.argb(220, 255, 255, 255)));
        linha1.addView(banner, lp(0, TODO, 1f));

        LinearLayout sugestoes = vertical();
        sugestoes.setBackground(criarVidro(22));
        sugestoes.setPadding(dp(12), dp(10), dp(12), dp(10));
        sugestoes.addView(texto("Sugestões para você", 13, BRANCO));
        for (int r = 0; r < 2; r++) {
            LinearLayout fila = horizontal();
            for (int k = 0; k < 3; k++) {
                View espaco = new View(this);
                espaco.setBackground(cor(35, 10));
                LinearLayout.LayoutParams pe = lp(0, TODO, 1f);
                pe.setMargins(dp(3), dp(3), dp(3), dp(3));
                fila.addView(espaco, pe);
            }
            sugestoes.addView(fila, lp(TODO, 0, 1f));
        }
        LinearLayout.LayoutParams paramSug = lp(dp(230), TODO);
        paramSug.leftMargin = dp(12);
        linha1.addView(sugestoes, paramSug);
        conteudo.addView(linha1, lp(TODO, 0, 3f));

        conteudo.addView(secao("Seus aplicativos favoritos"), lp(TODO, AUTO));
        conteudo.addView(criarFaixaApps(true), lp(TODO, 0, 2f));
        conteudo.addView(secao("Todos os aplicativos"), lp(TODO, AUTO));
        conteudo.addView(criarFaixaApps(false), lp(TODO, 0, 2f));
        return conteudo;
    }

    private View criarFaixaApps(boolean favoritos) {
        HorizontalScrollView faixa = new HorizontalScrollView(this);
        faixa.setHorizontalScrollBarEnabled(false);
        faixa.setClipToPadding(false);
        faixa.setPadding(dp(4), dp(6), dp(4), dp(6));

        LinearLayout linha = horizontal();
        linha.setClipChildren(false);
        int limite = favoritos ? Math.min(8, apps.size()) : apps.size();
        for (int i = 0; i < limite; i++) {
            LinearLayout.LayoutParams p = lp(dp(96), TODO);
            p.rightMargin = dp(10);
            linha.addView(criarTile(apps.get(i)), p);
        }
        if (favoritos) {
            LinearLayout mais = criarTileBase();
            mais.addView(texto("+", 28, BRANCO));
            mais.addView(texto("Adicionar", 11, BRANCO));
            mais.setOnClickListener(v -> avisar("Adicionar favorito"));
            linha.addView(mais, lp(dp(96), TODO));
        }
        faixa.addView(linha, new FrameLayout.LayoutParams(AUTO, TODO));
        return faixa;
    }

    private LinearLayout criarTileBase() {
        LinearLayout tile = vertical();
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(6), dp(6), dp(6), dp(6));
        tile.setBackground(fundoFoco(16, criarVidro(16)));
        tile.setFocusable(true);
        tile.setOnFocusChangeListener((v, tem) -> v.animate()
                .scaleX(tem ? 1.07f : 1f).scaleY(tem ? 1.07f : 1f)
                .setDuration(120).start());
        if (primeiroFoco == null) {
            primeiroFoco = tile;
        }
        return tile;
    }

    private View criarTile(final ResolveInfo info) {
        PackageManager pm = getPackageManager();
        LinearLayout tile = criarTileBase();

        ImageView icone = new ImageView(this);
        icone.setImageDrawable(info.loadIcon(pm));
        tile.addView(icone, lp(dp(44), dp(44)));

        TextView nome = texto(info.loadLabel(pm).toString(), 11, BRANCO);
        nome.setGravity(Gravity.CENTER);
        nome.setMaxLines(1);
        nome.setEllipsize(TextUtils.TruncateAt.END);
        nome.setPadding(0, dp(6), 0, 0);
        tile.addView(nome, lp(TODO, AUTO));

        tile.setOnClickListener(v -> abrir(info));
        return tile;
    }

    private TextView secao(String titulo) {
        TextView t = texto(titulo, 15, BRANCO);
        t.setPadding(dp(4), dp(8), 0, dp(2));
        return t;
    }

    private TextView texto(String conteudo, int sp, int corTexto) {
        TextView t = new TextView(this);
        t.setText(conteudo);
        t.setTextSize(sp);
        t.setTextColor(corTexto);
        return t;
    }

    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    private LinearLayout.LayoutParams lp(int largura, int altura) {
        return new LinearLayout.LayoutParams(largura, altura);
    }

    private LinearLayout.LayoutParams lp(int largura, int altura, float peso) {
        return new LinearLayout.LayoutParams(largura, altura, peso);
    }

    private Drawable cor(int alfa, int raio) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.argb(alfa, 255, 255, 255));
        d.setCornerRadius(dp(raio));
        return d;
    }

    private Drawable fundoFoco(int raio, Drawable normal) {
        GradientDrawable foco = new GradientDrawable();
        foco.setColor(Color.argb(70, 255, 255, 255));
        foco.setStroke(dp(2), Color.WHITE);
        foco.setCornerRadius(dp(raio));
        StateListDrawable estados = new StateListDrawable();
        estados.addState(new int[]{android.R.attr.state_focused}, foco);
        estados.addState(new int[]{}, normal);
        return estados;
    }

    private Drawable criarVidro(int raio) {
        GradientDrawable escuro = new GradientDrawable();
        escuro.setColor(Color.argb(40, 24, 22, 26));
        escuro.setCornerRadius(dp(raio));

        GradientDrawable brilho = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.argb(40, 255, 255, 255), Color.argb(8, 255, 255, 255)});
        brilho.setCornerRadius(dp(raio));
        brilho.setStroke(dp(1), Color.argb(90, 255, 255, 255));

        return new LayerDrawable(new Drawable[]{escuro, brilho});
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    private void avisar(String nome) {
        Toast.makeText(this, nome + ": em breve", Toast.LENGTH_SHORT).show();
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
    }
