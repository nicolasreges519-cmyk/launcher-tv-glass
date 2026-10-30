package com.meulauncher.glass;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final int TODO = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int AUTO = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int BRANCO = Color.WHITE;
    private static final int SUAVE = Color.argb(215, 255, 255, 255);
    private static final int AZUL = Color.rgb(150, 195, 255);

    // Troque pela sua cidade (usada só no widget de clima)
    private static final String CIDADE = "São Paulo";

    private static final int I_HOME = 0;
    private static final int I_GRADE = 1;
    private static final int I_PLAY = 2;
    private static final int I_NOTA = 3;
    private static final int I_ENGRENAGEM = 4;
    private static final int I_ESTRELA = 5;
    private static final int I_SETA = 6;
    private static final int I_JOGO = 7;
    private static final int I_TV = 8;
    private static final int I_LOJA = 9;
    private static final int I_AJUSTES = 10;
    private static final int I_CLIMA = 11;

    private static final String[] MENU_IDS = {
            "inicio", "apps", "filmes", "tv", "jogos", "musica", "loja", "ajustes", "personalizar"};
    private static final String[] MENU_NOMES = {
            "Início", "Apps", "Filmes e Séries", "TV ao Vivo", "Jogos", "Música",
            "Loja de apps", "Ajustes da TV", "Personalizar"};
    private static final int[] MENU_ICONES = {
            I_HOME, I_GRADE, I_PLAY, I_TV, I_JOGO, I_NOTA, I_LOJA, I_AJUSTES, I_ENGRENAGEM};

    // Apps que entram como favoritos na primeira vez (se estiverem instalados)
    private static final String[] FAVORITOS_PADRAO = {
            "netflix", "prime video", "disney", "youtube", "max",
            "globoplay", "apple tv", "paramount", "spotify"};

    private static final String[] K_FILMES = {
            "netflix", "prime video", "disney", "globoplay", "max", "apple tv", "paramount",
            "pluto", "star+", "telecine", "youtube", "claro", "discovery", "crunchyroll",
            "mubi", "tubi", "hulu", "vix", "sbt", "looke", "cine", "movie", "video", "filmes"};
    private static final String[] K_NAO_FILMES = {"music", "música", "spotify"};
    private static final String[] K_MUSICA = {
            "spotify", "deezer", "music", "música", "tidal", "radio", "rádio",
            "vagalume", "podcast"};
    private static final String[] K_JOGOS = {
            "xbox", "geforce", "stadia", "luna", "game", "jogo", "play games"};
    private static final String[] K_TV = {
            "tv", "globoplay", "pluto", "sbt", "claro", "telecine", "star+", "sky",
            "oi play", "ao vivo", "live"};

    // Foto de fundo: preenche a área cortando o excesso
    private static class FundoFoto extends Drawable {
        private final Bitmap foto;
        private final Paint tinta = new Paint(Paint.FILTER_BITMAP_FLAG);
        private final Rect origem = new Rect();

        FundoFoto(Bitmap foto) {
            this.foto = foto;
        }

        @Override
        public void draw(Canvas c) {
            Rect b = getBounds();
            if (b.width() <= 0 || b.height() <= 0) {
                return;
            }
            float escala = Math.max((float) b.width() / foto.getWidth(),
                    (float) b.height() / foto.getHeight());
            int largura = Math.round(b.width() / escala);
            int altura = Math.round(b.height() / escala);
            int esquerda = (foto.getWidth() - largura) / 2;
            int topo = (foto.getHeight() - altura) / 2;
            origem.set(esquerda, topo, esquerda + largura, topo + altura);
            c.drawBitmap(foto, origem, b, tinta);
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter filtro) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }

    // Plano B: paisagem de pôr do sol desenhada pelo código
    private static class Paisagem extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path pa = new Path();

        @Override
        public void draw(Canvas c) {
            Rect b = getBounds();
            float w = b.width();
            float h = b.height();
            c.save();
            c.translate(b.left, b.top);

            p.setShader(new LinearGradient(0, 0, 0, h * .56f,
                    new int[]{0xFF0F1B45, 0xFF353C8C, 0xFF8A5AA2, 0xFFE8904F, 0xFFF7C47C},
                    new float[]{0f, .3f, .55f, .82f, 1f}, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h * .56f, p);

            p.setShader(new RadialGradient(w * .68f, h * .5f, h * .36f,
                    new int[]{0xFFFFF1C2, 0xAAFFB35A, 0x00FFB35A},
                    new float[]{0f, .28f, 1f}, Shader.TileMode.CLAMP));
            c.drawCircle(w * .68f, h * .5f, h * .36f, p);

            p.setShader(null);
            p.setColor(0xFF4B3F88);
            pa.reset();
            pa.moveTo(0, h * .56f);
            pa.lineTo(0, h * .48f);
            pa.lineTo(w * .10f, h * .42f);
            pa.lineTo(w * .20f, h * .47f);
            pa.lineTo(w * .32f, h * .37f);
            pa.lineTo(w * .44f, h * .47f);
            pa.lineTo(w * .55f, h * .43f);
            pa.lineTo(w * .64f, h * .50f);
            pa.lineTo(w * .74f, h * .43f);
            pa.lineTo(w * .86f, h * .47f);
            pa.lineTo(w, h * .41f);
            pa.lineTo(w, h * .56f);
            pa.close();
            c.drawPath(pa, p);

            p.setShader(new LinearGradient(0, h * .56f, 0, h,
                    new int[]{0xFFE39A5E, 0xFF6C4F8E, 0xFF1B2B5E, 0xFF0A1230},
                    new float[]{0f, .25f, .6f, 1f}, Shader.TileMode.CLAMP));
            c.drawRect(0, h * .56f, w, h, p);

            c.restore();
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter filtro) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }

    // Ícones desenhados pelo código
    private static class IconeView extends View {
        private final int tipo;
        private final Paint tinta = new Paint(Paint.ANTI_ALIAS_FLAG);

        IconeView(Context contexto, int tipo) {
            super(contexto);
            this.tipo = tipo;
            tinta.setColor(Color.WHITE);
            tinta.setStrokeCap(Paint.Cap.ROUND);
            tinta.setStrokeJoin(Paint.Join.ROUND);
        }

        private void cheio() {
            tinta.setStyle(Paint.Style.FILL);
        }

        private void linha() {
            tinta.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onDraw(Canvas c) {
            float s = Math.min(getWidth(), getHeight());
            c.save();
            c.translate((getWidth() - s) / 2f, (getHeight() - s) / 2f);
            tinta.setStrokeWidth(s * 0.08f);
            switch (tipo) {
                case I_HOME: {
                    cheio();
                    Path p = new Path();
                    p.moveTo(s * .5f, s * .12f);
                    p.lineTo(s * .92f, s * .5f);
                    p.lineTo(s * .78f, s * .5f);
                    p.lineTo(s * .78f, s * .88f);
                    p.lineTo(s * .22f, s * .88f);
                    p.lineTo(s * .22f, s * .5f);
                    p.lineTo(s * .08f, s * .5f);
                    p.close();
                    c.drawPath(p, tinta);
                    break;
                }
                case I_GRADE: {
                    cheio();
                    float r = s * .08f;
                    c.drawRoundRect(new RectF(s * .12f, s * .12f, s * .44f, s * .44f), r, r, tinta);
                    c.drawRoundRect(new RectF(s * .56f, s * .12f, s * .88f, s * .44f), r, r, tinta);
                    c.drawRoundRect(new RectF(s * .12f, s * .56f, s * .44f, s * .88f), r, r, tinta);
                    c.drawRoundRect(new RectF(s * .56f, s * .56f, s * .88f, s * .88f), r, r, tinta);
                    break;
                }
                case I_PLAY: {
                    cheio();
                    Path t = new Path();
                    t.moveTo(s * .3f, s * .2f);
                    t.lineTo(s * .3f, s * .8f);
                    t.lineTo(s * .82f, s * .5f);
                    t.close();
                    c.drawPath(t, tinta);
                    break;
                }
                case I_NOTA: {
                    cheio();
                    c.drawCircle(s * .35f, s * .72f, s * .14f, tinta);
                    c.drawRect(s * .44f, s * .2f, s * .52f, s * .72f, tinta);
                    c.drawRect(s * .44f, s * .2f, s * .78f, s * .32f, tinta);
                    break;
                }
                case I_ENGRENAGEM: {
                    cheio();
                    c.drawCircle(s * .5f, s * .5f, s * .26f, tinta);
                    linha();
                    for (int i = 0; i < 8; i++) {
                        double a = Math.PI * i / 4;
                        float cx = (float) Math.cos(a);
                        float cy = (float) Math.sin(a);
                        c.drawLine(s * .5f + cx * s * .32f, s * .5f + cy * s * .32f,
                                s * .5f + cx * s * .43f, s * .5f + cy * s * .43f, tinta);
                    }
                    break;
                }
                case I_ESTRELA: {
                    cheio();
                    Path p = new Path();
                    for (int i = 0; i < 10; i++) {
                        double a = -Math.PI / 2 + Math.PI * i / 5;
                        float r = (i % 2 == 0 ? .42f : .18f) * s;
                        float x = s * .5f + (float) Math.cos(a) * r;
                        float y = s * .54f + (float) Math.sin(a) * r;
                        if (i == 0) {
                            p.moveTo(x, y);
                        } else {
                            p.lineTo(x, y);
                        }
                    }
                    p.close();
                    c.drawPath(p, tinta);
                    break;
                }
                case I_SETA: {
                    linha();
                    c.drawLine(s * .4f, s * .25f, s * .65f, s * .5f, tinta);
                    c.drawLine(s * .65f, s * .5f, s * .4f, s * .75f, tinta);
                    break;
                }
                case I_JOGO: {
                    cheio();
                    c.drawRoundRect(new RectF(s * .08f, s * .28f, s * .92f, s * .72f),
                            s * .2f, s * .2f, tinta);
                    break;
                }
                case I_TV: {
                    linha();
                    c.drawRoundRect(new RectF(s * .1f, s * .18f, s * .9f, s * .68f),
                            s * .08f, s * .08f, tinta);
                    c.drawLine(s * .5f, s * .68f, s * .5f, s * .82f, tinta);
                    c.drawLine(s * .3f, s * .84f, s * .7f, s * .84f, tinta);
                    break;
                }
                case I_AJUSTES: {
                    linha();
                    c.drawRoundRect(new RectF(s * .1f, s * .18f, s * .9f, s * .68f),
                            s * .08f, s * .08f, tinta);
                    c.drawLine(s * .5f, s * .68f, s * .5f, s * .82f, tinta);
                    c.drawLine(s * .3f, s * .84f, s * .7f, s * .84f, tinta);
                    cheio();
                    c.drawCircle(s * .5f, s * .43f, s * .1f, tinta);
                    break;
                }
                case I_LOJA: {
                    linha();
                    c.drawRoundRect(new RectF(s * .18f, s * .36f, s * .82f, s * .88f),
                            s * .08f, s * .08f, tinta);
                    c.drawArc(new RectF(s * .32f, s * .12f, s * .68f, s * .56f),
                            180f, 180f, false, tinta);
                    break;
                }
                case I_CLIMA: {
                    cheio();
                    c.drawCircle(s * .5f, s * .5f, s * .44f, tinta);
                    break;
                }
                default:
                    break;
            }
            c.restore();
        }
    }

    // Vidro fosco: fundo borrado atrás, névoa, brilho e borda luminosa
    private class Vidro extends Drawable {
        private final float raio;
        private final Paint pCeu = new Paint(Paint.FILTER_BITMAP_FLAG);
        private final Paint pBruma = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pBrilho = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pBorda = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path forma = new Path();
        private final RectF area = new RectF();
        private final RectF destino = new RectF();

        Vidro(int raioU) {
            this.raio = u(raioU);
            pBruma.setStyle(Paint.Style.FILL);
            pBruma.setColor(Color.argb(38, 255, 255, 255));
            pBrilho.setStyle(Paint.Style.FILL);
            pBorda.setStyle(Paint.Style.STROKE);
            pBorda.setStrokeWidth(Math.max(2f, u(3)));
        }

        @Override
        protected void onBoundsChange(Rect b) {
            area.set(b);
            area.inset(u(2), u(2));
            forma.reset();
            forma.addRoundRect(area, raio, raio, Path.Direction.CW);
            pBrilho.setShader(new LinearGradient(area.left, area.top, area.right, area.bottom,
                    new int[]{Color.argb(90, 255, 255, 255), Color.argb(8, 255, 255, 255),
                            Color.argb(45, 255, 255, 255)},
                    new float[]{0f, .55f, 1f}, Shader.TileMode.CLAMP));
            pBorda.setShader(new LinearGradient(area.left, area.top, area.right, area.bottom,
                    new int[]{Color.argb(235, 255, 255, 255), Color.argb(50, 255, 255, 255),
                            Color.argb(150, 255, 255, 255)},
                    new float[]{0f, .5f, 1f}, Shader.TileMode.CLAMP));
        }

        private View encontrarDono() {
            Drawable.Callback cb = getCallback();
            while (cb instanceof Drawable) {
                cb = ((Drawable) cb).getCallback();
            }
            return cb instanceof View ? (View) cb : null;
        }

        @Override
        public void draw(Canvas c) {
            c.save();
            c.clipPath(forma);
            View dono = encontrarDono();
            if (foscoLigado && dono != null && fundoDesfocado != null) {
                View janela = dono.getRootView();
                int[] a = new int[2];
                int[] j = new int[2];
                dono.getLocationInWindow(a);
                janela.getLocationInWindow(j);
                c.save();
                c.translate(j[0] - a[0], j[1] - a[1]);
                destino.set(0, 0, janela.getWidth(), janela.getHeight());
                c.drawBitmap(fundoDesfocado, null, destino, pCeu);
                c.restore();
            }
            c.drawRect(area, pBruma);
            c.drawPath(forma, pBrilho);
            c.restore();
            c.drawPath(forma, pBorda);
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter filtro) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private final List<ResolveInfo> apps = new ArrayList<>();
    private final List<String> favoritos = new ArrayList<>();
    private final Map<String, List<View>> estrelas = new HashMap<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private float escala = 1f;
    private SharedPreferences prefs;
    private boolean foscoLigado = true;
    private boolean nomesLigados = true;
    private Bitmap foto;
    private Bitmap fundoDesfocado;
    private FrameLayout raiz;
    private FrameLayout tela;
    private String telaAtual = "inicio";
    private View menuAtivo;
    private View focoInicial;

    private TextView relogioTexto;
    private TextView dataTexto;

    private TextView heroRotulo;
    private TextView heroTitulo;
    private TextView heroSub;
    private TextView heroBotaoTexto;
    private TextView heroChip;
    private View heroIcone;
    private ResolveInfo heroInfo;

    private LinearLayout linhaFavoritos;
    private LinearLayout linhaTodos;

    private TextView climaCidadeView;
    private TextView climaTempView;
    private TextView climaDescView;
    private String climaCidade = "Sua cidade";
    private String climaTemp = "--°";
    private String climaDesc = "Carregando...";

    private final Runnable ciclo = new Runnable() {
        @Override
        public void run() {
            atualizarRelogio();
            handler.postDelayed(this, 15000);
        }
    };

    // Converte uma medida do quadro de design (1920 x 1080) para pixels da TV
    private int u(float valor) {
        return Math.round(valor * escala);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics m = getResources().getDisplayMetrics();
        escala = Math.min(m.widthPixels / 1920f, m.heightPixels / 1080f);

        prefs = getSharedPreferences("launcher", MODE_PRIVATE);
        foscoLigado = prefs.getBoolean("fosco", true);
        nomesLigados = prefs.getBoolean("nomes", true);

        carregarApps();
        carregarFavoritos();

        foto = carregarFoto();
        fundoDesfocado = criarFundoDesfocado();

        raiz = new FrameLayout(this);
        raiz.setBackground(fundoTela());

        tela = new FrameLayout(this);
        tela.setClipChildren(false);
        raiz.addView(tela, new FrameLayout.LayoutParams(u(1920), u(1080), Gravity.CENTER));
        setContentView(raiz);

        mostrarTela("inicio");
        new Thread(this::carregarClima).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(ciclo);
        handler.post(ciclo);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ciclo);
    }

    @Override
    public void onBackPressed() {
        if (!"inicio".equals(telaAtual)) {
            mostrarTela("inicio");
        } else {
            super.onBackPressed();
        }
    }

    // ---------- Telas ----------

    private void mostrarTela(String id) {
        telaAtual = id;
        tela.removeAllViews();
        estrelas.clear();
        menuAtivo = null;
        focoInicial = null;
        heroTitulo = null;
        heroInfo = null;
        linhaFavoritos = null;
        linhaTodos = null;
        climaCidadeView = null;

        colocar(criarRelogio(), 100, 64, 520, 200);
        colocar(criarMenu(id), 84, 280, 430, 680);
        colocar(criarDica(), 100, 975, 420, 80);

        switch (id) {
            case "inicio":
                montarInicio();
                break;
            case "personalizar":
                montarPersonalizar();
                break;
            case "entradas":
                montarEntradas();
                break;
            default:
                montarPainelApps(id);
                break;
        }

        atualizarRelogio();
        if (menuAtivo != null) {
            menuAtivo.requestFocus();
        } else if (focoInicial != null) {
            focoInicial.requestFocus();
        }
    }

    // Coloca uma peça numa posição do quadro de design
    private void colocar(View v, int x, int y, int w, int h) {
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(u(w), u(h));
        p.leftMargin = u(x);
        p.topMargin = u(y);
        tela.addView(v, p);
    }

    private void montarInicio() {
        colocar(criarHero(), 545, 215, 1030, 340);
        colocar(criarWidgetClima(), 1600, 215, 270, 170);
        colocar(criarWidgetEntradas(), 1600, 405, 270, 150);
        colocar(secao("Seus aplicativos"), 555, 574, 600, 40);
        colocar(criarFaixaFavoritos(), 545, 618, 1335, 196);
        colocar(secao("Todos os apps"), 555, 828, 600, 40);
        colocar(criarFaixaTodos(), 545, 872, 1335, 196);
        heroPadrao();
    }

    // ---------- Relógio, menu e dica ----------

    private View criarRelogio() {
        LinearLayout bloco = vertical();
        relogioTexto = texto("--:--", 112, BRANCO);
        relogioTexto.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        relogioTexto.setIncludeFontPadding(false);
        bloco.addView(relogioTexto);
        dataTexto = texto("", 34, SUAVE);
        dataTexto.setPadding(u(4), u(6), 0, 0);
        bloco.addView(dataTexto);
        return bloco;
    }

    private void atualizarRelogio() {
        if (relogioTexto == null || dataTexto == null) {
            return;
        }
        Calendar c = Calendar.getInstance();
        relogioTexto.setText(String.format(Locale.US, "%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
        String[] dias = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        String[] meses = {"janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho",
                "agosto", "setembro", "outubro", "novembro", "dezembro"};
        dataTexto.setText(dias[c.get(Calendar.DAY_OF_WEEK) - 1] + ", "
                + c.get(Calendar.DAY_OF_MONTH) + " de " + meses[c.get(Calendar.MONTH)]);
    }

    private View criarMenu(String ativo) {
        LinearLayout menu = vertical();
        menu.setBackground(criarVidro(48));
        menu.setPadding(u(20), u(20), u(20), u(20));
        for (int i = 0; i < MENU_IDS.length; i++) {
            final String id = MENU_IDS[i];
            boolean ehAtivo = id.equals(ativo);

            LinearLayout item = horizontal();
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(u(24), 0, u(16), 0);
            item.setFocusable(true);
            item.setBackground(fundoFoco(34, ehAtivo ? ativoDrawable(34) : cor(0, 34)));
            item.addView(new IconeView(this, MENU_ICONES[i]), lp(u(40), u(40)));
            TextView t = texto(MENU_NOMES[i], 32, BRANCO);
            t.setMaxLines(1);
            t.setPadding(u(20), 0, 0, 0);
            item.addView(t);
            item.setOnClickListener(v -> abrirItemMenu(id));
            item.setOnFocusChangeListener((v, tem) -> {
                if (tem && "inicio".equals(telaAtual)) {
                    heroPadrao();
                }
            });
            if (ehAtivo) {
                menuAtivo = item;
            }
            LinearLayout.LayoutParams p = lp(TODO, u(64));
            p.bottomMargin = u(8);
            menu.addView(item, p);
        }
        return menu;
    }

    private void abrirItemMenu(String id) {
        if ("loja".equals(id)) {
            abrirLoja();
        } else if ("ajustes".equals(id)) {
            abrirAjustes();
        } else {
            mostrarTela(id);
        }
    }

    private View criarDica() {
        LinearLayout dica = vertical();
        dica.addView(texto("↑ ↓  navegar", 24, SUAVE));
        TextView segunda = texto("OK abrir  •  segure OK: favoritar", 24, SUAVE);
        segunda.setPadding(0, u(6), 0, 0);
        dica.addView(segunda);
        return dica;
    }

    // ---------- Painel principal (Início) ----------

    private View criarHero() {
        LinearLayout hero = vertical();
        hero.setBackground(criarVidro(44));
        hero.setPadding(u(56), u(34), u(40), u(30));

        heroRotulo = texto("INÍCIO", 24, SUAVE);
        hero.addView(heroRotulo);

        heroTitulo = texto("Boa tarde", 84, BRANCO);
        heroTitulo.setTypeface(null, Typeface.BOLD);
        heroTitulo.setMaxLines(1);
        heroTitulo.setEllipsize(TextUtils.TruncateAt.END);
        heroTitulo.setPadding(0, u(6), 0, u(4));
        hero.addView(heroTitulo);

        heroSub = texto("", 32, SUAVE);
        heroSub.setMaxLines(1);
        heroSub.setEllipsize(TextUtils.TruncateAt.END);
        hero.addView(heroSub);

        hero.addView(new View(this), lp(TODO, 0, 1f));

        LinearLayout botao = horizontal();
        botao.setGravity(Gravity.CENTER_VERTICAL);
        botao.setPadding(u(30), 0, u(34), 0);
        botao.setFocusable(true);
        botao.setBackground(fundoFoco(32, criarVidro(32)));
        heroIcone = new IconeView(this, I_PLAY);
        botao.addView(heroIcone, lp(u(30), u(30)));
        heroChip = texto("OK", 22, BRANCO);
        heroChip.setPadding(u(12), u(4), u(12), u(4));
        heroChip.setBackground(cor(70, 12));
        heroChip.setVisibility(View.GONE);
        botao.addView(heroChip);
        heroBotaoTexto = texto("Escolher um app", 28, BRANCO);
        heroBotaoTexto.setPadding(u(14), 0, 0, 0);
        botao.addView(heroBotaoTexto);
        botao.setOnClickListener(v -> {
            if (heroInfo != null) {
                abrir(heroInfo);
            } else {
                View alvo = primeiroTile(linhaFavoritos);
                if (alvo == null) {
                    alvo = primeiroTile(linhaTodos);
                }
                if (alvo != null) {
                    alvo.requestFocus();
                }
            }
        });
        hero.addView(botao, lp(AUTO, u(64)));
        return hero;
    }

    private String saudacao() {
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hora < 5) {
            return "Boa noite";
        }
        if (hora < 12) {
            return "Bom dia";
        }
        if (hora < 18) {
            return "Boa tarde";
        }
        return "Boa noite";
    }

    private void heroPadrao() {
        if (heroTitulo == null) {
            return;
        }
        heroInfo = null;
        heroRotulo.setText("INÍCIO");
        heroTitulo.setText(saudacao());
        heroSub.setText("Seus apps, do seu jeito, sem propaganda.");
        heroBotaoTexto.setText("Escolher um app");
        heroChip.setVisibility(View.GONE);
        heroIcone.setVisibility(View.VISIBLE);
    }

    private void heroParaApp(ResolveInfo info, String origem) {
        if (heroTitulo == null || !"inicio".equals(telaAtual)) {
            return;
        }
        heroInfo = info;
        heroRotulo.setText(origem);
        heroTitulo.setText(info.loadLabel(getPackageManager()));
        heroSub.setText(categoriaTexto(info));
        heroBotaoTexto.setText("Abrir");
        heroChip.setVisibility(View.VISIBLE);
        heroIcone.setVisibility(View.GONE);
    }

    private View primeiroTile(LinearLayout linha) {
        if (linha != null && linha.getChildCount() > 0) {
            View celula = linha.getChildAt(0);
            if (celula instanceof LinearLayout && ((LinearLayout) celula).getChildCount() > 0) {
                return ((LinearLayout) celula).getChildAt(0);
            }
        }
        return null;
    }

    // ---------- Widgets ----------

    private View criarWidgetClima() {
        LinearLayout widget = horizontal();
        widget.
