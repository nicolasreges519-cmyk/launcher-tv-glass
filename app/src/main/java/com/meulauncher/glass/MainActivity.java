package com.meulauncher.glass;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final int TODO = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int AUTO = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int BRANCO = Color.WHITE;
    private static final int SUAVE = Color.argb(215, 255, 255, 255);
    private static final int AZUL = Color.rgb(150, 195, 255);

    // true = vidro fosco com fundo borrado; false = vidro simples (mais leve)
    private static final boolean EFEITO_FOSCO = true;

    private static final int I_HOME = 0;
    private static final int I_GRADE = 1;
    private static final int I_PLAY = 2;
    private static final int I_NOTA = 3;
    private static final int I_ENGRENAGEM = 4;
    private static final int I_ESTRELA = 5;
    private static final int I_BUSCA = 6;
    private static final int I_WIFI = 7;
    private static final int I_BOLA = 8;
    private static final int I_JOGO = 9;
    private static final int I_SETA = 10;
    private static final int I_CONTROLE = 11;
    private static final int I_ANEL = 12;

    private static final int SEGUNDOS_POR_SUGESTAO = 5;

    // Apps que entram como favoritos na primeira vez (se estiverem instalados)
    private static final String[] FAVORITOS_PADRAO = {
            "netflix", "youtube", "disney", "prime video", "max",
            "globoplay", "spotify", "apple tv", "youtube kids"};

    private static class Sugestao {
        final String tipo;
        final String titulo;
        final String sinopse;
        final String detalhe;
        final String videoId;
        final Bitmap fundo;
        final Bitmap cartaz;

        Sugestao(String tipo, String titulo, String sinopse, String detalhe,
                 String videoId, Bitmap fundo, Bitmap cartaz) {
            this.tipo = tipo;
            this.titulo = titulo;
            this.sinopse = sinopse;
            this.detalhe = detalhe;
            this.videoId = videoId;
            this.fundo = fundo;
            this.cartaz = cartaz;
        }
    }

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

            p.setColor(0xFF2E2A66);
            pa.reset();
            pa.moveTo(0, h * .56f);
            pa.lineTo(0, h * .52f);
            pa.lineTo(w * .15f, h * .50f);
            pa.lineTo(w * .30f, h * .53f);
            pa.lineTo(w * .50f, h * .50f);
            pa.lineTo(w * .70f, h * .54f);
            pa.lineTo(w * .85f, h * .51f);
            pa.lineTo(w, h * .53f);
            pa.lineTo(w, h * .56f);
            pa.close();
            c.drawPath(pa, p);

            p.setShader(new LinearGradient(0, h * .56f, 0, h,
                    new int[]{0xFFE39A5E, 0xFF6C4F8E, 0xFF1B2B5E, 0xFF0A1230},
                    new float[]{0f, .25f, .6f, 1f}, Shader.TileMode.CLAMP));
            c.drawRect(0, h * .56f, w, h, p);

            p.setShader(new LinearGradient(0, h * .56f, 0, h * .9f,
                    new int[]{0xAAFFC27A, 0x00FFC27A}, null, Shader.TileMode.CLAMP));
            c.drawRect(w * .60f, h * .56f, w * .76f, h * .9f, p);

            p.setShader(null);
            p.setColor(0xCC070C22);
            pa.reset();
            pa.moveTo(0, h * .36f);
            pa.lineTo(w * .05f, h * .52f);
            pa.lineTo(w * .03f, h * .70f);
            pa.lineTo(0, h * .78f);
            pa.close();
            c.drawPath(pa, p);
            pa.reset();
            pa.moveTo(w, h * .34f);
            pa.lineTo(w * .94f, h * .50f);
            pa.lineTo(w * .96f, h * .72f);
            pa.lineTo(w, h * .80f);
            pa.close();
            c.drawPath(pa, p);

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
                    linha();
                    float r = s * .08f;
                    c.drawRoundRect(new RectF(s * .12f, s * .12f, s * .44f, s * .44f), r, r, tinta);
                    c.drawRoundRect(new RectF(s * .56f, s * .12f, s * .88f, s * .44f), r, r, tinta);
                    c.drawRoundRect(new RectF(s * .12f, s * .56f, s * .44f, s * .88f), r, r, tinta);
                    c.drawRoundRect(new RectF(s * .56f, s * .56f, s * .88f, s * .88f), r, r, tinta);
                    break;
                }
                case I_PLAY: {
                    linha();
                    c.drawCircle(s * .5f, s * .5f, s * .38f, tinta);
                    cheio();
                    Path t = new Path();
                    t.moveTo(s * .43f, s * .33f);
                    t.lineTo(s * .43f, s * .67f);
                    t.lineTo(s * .69f, s * .5f);
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
                    linha();
                    c.drawCircle(s * .5f, s * .5f, s * .2f, tinta);
                    for (int i = 0; i < 8; i++) {
                        double a = Math.PI * i / 4;
                        float cx = (float) Math.cos(a);
                        float cy = (float) Math.sin(a);
                        c.drawLine(s * .5f + cx * s * .3f, s * .5f + cy * s * .3f,
                                s * .5f + cx * s * .4f, s * .5f + cy * s * .4f, tinta);
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
                case I_BUSCA: {
                    linha();
                    c.drawCircle(s * .42f, s * .42f, s * .26f, tinta);
                    c.drawLine(s * .62f, s * .62f, s * .86f, s * .86f, tinta);
                    break;
                }
                case I_WIFI: {
                    linha();
                    float[] raios = {.22f, .42f, .62f};
                    for (float r : raios) {
                        c.drawArc(new RectF(s * (.5f - r), s * (.8f - r),
                                s * (.5f + r), s * (.8f + r)), -135f, 90f, false, tinta);
                    }
                    cheio();
                    c.drawCircle(s * .5f, s * .8f, s * .06f, tinta);
                    break;
                }
                case I_BOLA: {
                    linha();
                    c.drawCircle(s * .5f, s * .5f, s * .38f, tinta);
                    cheio();
                    Path p = new Path();
                    for (int i = 0; i < 5; i++) {
                        double a = -Math.PI / 2 + 2 * Math.PI * i / 5;
                        float x = s * .5f + (float) Math.cos(a) * s * .13f;
                        float y = s * .5f + (float) Math.sin(a) * s * .13f;
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
                case I_JOGO: {
                    linha();
                    c.drawRoundRect(new RectF(s * .1f, s * .3f, s * .9f, s * .7f),
                            s * .18f, s * .18f, tinta);
                    c.drawLine(s * .28f, s * .5f, s * .4f, s * .5f, tinta);
                    c.drawLine(s * .34f, s * .44f, s * .34f, s * .56f, tinta);
                    cheio();
                    c.drawCircle(s * .62f, s * .5f, s * .04f, tinta);
                    c.drawCircle(s * .72f, s * .44f, s * .04f, tinta);
                    break;
                }
                case I_SETA: {
                    linha();
                    c.drawLine(s * .4f, s * .25f, s * .65f, s * .5f, tinta);
                    c.drawLine(s * .65f, s * .5f, s * .4f, s * .75f, tinta);
                    break;
                }
                case I_CONTROLE: {
                    linha();
                    c.drawRoundRect(new RectF(s * .3f, s * .08f, s * .7f, s * .92f),
                            s * .12f, s * .12f, tinta);
                    cheio();
                    c.drawCircle(s * .5f, s * .3f, s * .07f, tinta);
                    break;
                }
                case I_ANEL: {
                    linha();
                    c.drawCircle(s * .5f, s * .5f, s * .36f, tinta);
                    c.drawCircle(s * .5f, s * .5f, s * .18f, tinta);
                    break;
                }
                default:
                    break;
            }
            c.restore();
        }
    }

    // Vidro fosco: mostra o fundo borrado, névoa, brilho e borda luminosa
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
            pBruma.setColor(Color.argb(34, 255, 255, 255));
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
            if (EFEITO_FOSCO && dono != null && fundoDesfocado != null) {
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
    private final List<Sugestao> sugestoes = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private float escala = 1f;
    private SharedPreferences prefs;
    private Bitmap foto;
    private Bitmap fundoDesfocado;
    private FrameLayout raiz;
    private FrameLayout tela;
    private LinearLayout linhaFavoritos;
    private FrameLayout overlay;
    private boolean modoFavoritos = false;
    private View primeiroFoco;
    private ImageView imagemCartaz;
    private TextView tituloSug;
    private TextView detalheSug;
    private TextView sinopseSug;
    private TextView rotuloBotao;
    private Sugestao atual;
    private int indice = 0;

    private final Runnable girar = new Runnable() {
        @Override
        public void run() {
            if (!sugestoes.isEmpty()) {
                indice = (indice + 1) % sugestoes.size();
                mostrar(sugestoes.get(indice));
            }
            handler.postDelayed(this, SEGUNDOS_POR_SUGESTAO * 1000L);
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

        carregarApps();
        carregarFavoritos();

        foto = carregarFoto();
        fundoDesfocado = criarFundoDesfocado();

        raiz = new FrameLayout(this);
        raiz.setBackground(fundoTela());

        tela = new FrameLayout(this);
        tela.setClipChildren(false);
        raiz.addView(tela, new FrameLayout.LayoutParams(u(1920), u(1080), Gravity.CENTER));

        colocar(criarMarca(), 66, 56, 290, 66);
        colocar(criarMenuTopo(), 360, 56, 1061, 66);
        colocar(criarRelogio(), 1520, 50, 346, 78);
        colocar(criarLateral(), 54, 172, 235, 766);
        colocar(criarPainelSugestoes(), 1010, 172, 856, 358);
        colocar(secao("Seus aplicativos favoritos"), 312, 546, 900, 40);
        colocar(criarFaixaFavoritos(), 302, 588, 1574, 164);
        colocar(secao("Todos os aplicativos"), 312, 764, 900, 40);
        colocar(criarCategorias(), 312, 808, 1554, 124);
        colocar(criarRodape(), 54, 972, 1812, 58);

        setContentView(raiz);
        if (primeiroFoco != null) {
            primeiroFoco.requestFocus();
        }

        new Thread(this::carregarSugestoes).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(girar);
        if (!sugestoes.isEmpty()) {
            handler.postDelayed(girar, SEGUNDOS_POR_SUGESTAO * 1000L);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(girar);
    }

    @Override
    public void onBackPressed() {
        if (overlay != null) {
            fecharTodosApps();
        } else {
            super.onBackPressed();
        }
    }

    // Coloca uma peça numa posição do quadro de design
    private void colocar(View v, int x, int y, int w, int h) {
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(u(w), u(h));
        p.leftMargin = u(x);
        p.topMargin = u(y);
        tela.addView(v, p);
    }

    private Bitmap carregarFoto() {
        try (InputStream entrada = getAssets().open("fundo.jpg")) {
            return BitmapFactory.decodeStream(entrada);
        } catch (Exception e) {
            return null;
        }
    }

    private Drawable fundoTela() {
        if (foto != null) {
            return new FundoFoto(foto);
        }
        return new Paisagem();
    }

    private Bitmap reduzir(Bitmap origem, int larguraAlvo) {
        Bitmap atualBitmap = origem;
        while (atualBitmap.getWidth() / 2 >= larguraAlvo) {
            atualBitmap = Bitmap.createScaledBitmap(atualBitmap, atualBitmap.getWidth() / 2,
                    atualBitmap.getHeight() / 2, true);
        }
        return atualBitmap;
    }

    private Bitmap criarFundoDesfocado() {
        Bitmap medio = Bitmap.createBitmap(96, 54, Bitmap.Config.ARGB_8888);
        Drawable base;
        if (foto != null) {
            base = new FundoFoto(reduzir(foto, 100));
        } else {
            base = new Paisagem();
        }
        base.setBounds(0, 0, 96, 54);
        base.draw(new Canvas(medio));
        return Bitmap.createScaledBitmap(medio, 48, 27, true);
    }

    private void arredondar(View v, final int raioU) {
        v.setClipToOutline(true);
        v.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), u(raioU));
            }
        });
    }

    // ---------- Barra superior ----------

    private View criarMarca() {
        LinearLayout marca = horizontal();
        marca.setGravity(Gravity.CENTER_VERTICAL);
        TextView tcl = texto("TCL", 44, BRANCO);
        tcl.setTypeface(null, Typeface.BOLD);
        marca.addView(tcl);
        TextView googleTv = texto("Google TV", 40, BRANCO);
        googleTv.setPadding(u(10), 0, 0, 0);
        marca.addView(googleTv);
        return marca;
    }

    private LinearLayout item(int tipo, String nome, boolean ativo, int raio,
                              float textoPx, float iconePx, boolean centro, final Runnable acao) {
        LinearLayout l = horizontal();
        l.setGravity(centro ? Gravity.CENTER : (Gravity.CENTER_VERTICAL | Gravity.START));
        l.setPadding(u(22), 0, u(14), 0);
        l.setFocusable(true);
        l.setBackground(fundoFoco(raio, cor(ativo ? 48 : 0, raio)));
        l.addView(new IconeView(this, tipo), lp(u(iconePx), u(iconePx)));
        TextView t = texto(nome, textoPx, BRANCO);
        t.setMaxLines(1);
        t.setPadding(u(14), 0, 0, 0);
        l.addView(t);
        l.setOnClickListener(v -> acao.run());
        return l;
    }

    private View criarMenuTopo() {
        LinearLayout menu = horizontal();
        menu.setGravity(Gravity.CENTER_VERTICAL);
        menu.setBackground(criarVidro(33));
        menu.setPadding(u(10), u(8), u(10), u(8));
        menu.addView(item(I_HOME, "Início", true, 26, 24, 32, true,
                () -> avisar("Início")), lp(0, TODO, 1f));
        menu.addView(item(I_GRADE, "Aplicativos", false, 26, 24, 32, true,
                () -> abrirTodosApps(false)), lp(0, TODO, 1f));
        menu.addView(item(I_PLAY, "Filmes e Séries", false, 26, 24, 32, true,
                () -> avisar("Filmes e Séries")), lp(0, TODO, 1f));
        menu.addView(item(I_NOTA, "Música", false, 26, 24, 32, true,
                () -> avisar("Música")), lp(0, TODO, 1f));
        menu.addView(item(I_ENGRENAGEM, "Configurações", false, 26, 24, 32, true,
                () -> avisar("Configurações")), lp(0, TODO, 1f));
        return menu;
    }

    private View criarRelogio() {
        LinearLayout direita = horizontal();
        direita.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        direita.addView(new IconeView(this, I_WIFI), lp(u(40), u(40)));

        LinearLayout relogio = vertical();
        relogio.setGravity(Gravity.END);
        relogio.setPadding(u(22), 0, 0, 0);
        TextClock hora = new TextClock(this);
        hora.setFormat24Hour("HH:mm");
        hora.setFormat12Hour("HH:mm");
        hora.setTextColor(BRANCO);
        hora.setTextSize(TypedValue.COMPLEX_UNIT_PX, u(44));
        relogio.addView(hora);
        TextClock data = new TextClock(this);
        data.setFormat24Hour("EEE, d 'de' MMM");
        data.setFormat12Hour("EEE, d 'de' MMM");
        data.setTextColor(SUAVE);
        data.setTextSize(TypedValue.COMPLEX_UNIT_PX, u(22));
        relogio.addView(data);
        direita.addView(relogio);
        return direita;
    }

    // ---------- Menu lateral ----------

    private View criarLateral() {
        LinearLayout lateral = vertical();
        lateral.setBackground(criarVidro(34));
        lateral.setPadding(u(16), u(34), u(16), u(20));
        View[] itens = {
                item(I_HOME, "Início", true, 26, 26, 38, false, () -> avisar("Início")),
                item(I_GRADE, "Meus Apps", false, 26, 26, 38, false, () -> abrirTodosApps(false)),
                item(I_ESTRELA, "Favoritos", false, 26, 26, 38, false, () -> abrirTodosApps(true)),
                item(I_BUSCA, "Pesquisar", false, 26, 26, 38, false, () -> avisar("Pesquisar")),
                item(I_ENGRENAGEM, "Configurações", false, 26, 26, 38, false, () -> avisar("Configurações"))
        };
        for (View v : itens) {
            LinearLayout.LayoutParams p = lp(TODO, u(64));
            p.bottomMargin = u(9);
            lateral.addView(v, p);
        }
        return lateral;
    }

    // ---------- Painel do canto: cartaz dinâmico + descrição ----------

    private View criarPainelSugestoes() {
        LinearLayout painel = vertical();
        painel.setBackground(criarVidro(34));
        painel.setPadding(u(26), u(20), u(26), u(24));

        LinearLayout cabecalho = horizontal();
        cabecalho.setGravity(Gravity.CENTER_VERTICAL);
        cabecalho.addView(texto("Sugestões para você", 28, BRANCO), lp(0, AUTO, 1f));
        cabecalho.addView(new IconeView(this, I_SETA), lp(u(26), u(26)));
        painel.addView(cabecalho, lp(TODO, u(40)));

        LinearLayout corpo = horizontal();

        FrameLayout moldura = new FrameLayout(this);
        moldura.setBackground(cor(25, 18));
        arredondar(moldura, 18);
        imagemCartaz = new ImageView(this);
        imagemCartaz.setScaleType(ImageView.ScaleType.CENTER_CROP);
        moldura.addView(imagemCartaz, new FrameLayout.LayoutParams(TODO, TODO));
        corpo.addView(moldura, lp(u(172), TODO));

        LinearLayout textos = vertical();
        textos.setPadding(u(28), 0, 0, 0);

        tituloSug = texto("Carregando sugestões...", 32, BRANCO);
        tituloSug.setTypeface(null, Typeface.BOLD);
        tituloSug.setMaxLines(2);
        tituloSug.setEllipsize(TextUtils.TruncateAt.END);
        textos.addView(tituloSug);

        detalheSug = texto("", 18, SUAVE);
        detalheSug.setPadding(0, u(4), 0, 0);
        textos.addView(detalheSug);

        sinopseSug = texto("", 20, SUAVE);
        sinopseSug.setMaxLines(3);
        sinopseSug.setEllipsize(TextUtils.TruncateAt.END);
        sinopseSug.setPadding(0, u(10), 0, 0);
        textos.addView(sinopseSug);

        textos.addView(new View(this), lp(TODO, 0, 1f));

        LinearLayout botao = horizontal();
        botao.setGravity(Gravity.CENTER_VERTICAL);
        botao.setPadding(u(28), 0, u(32), 0);
        botao.setFocusable(true);
        botao.setBackground(fundoFoco(26, criarVidro(26)));
        botao.addView(new IconeView(this, I_PLAY), lp(u(26), u(26)));
        rotuloBotao = texto("Ver mais", 24, BRANCO);
        rotuloBotao.setPadding(u(12), 0, 0, 0);
        botao.addView(rotuloBotao);
        botao.setOnClickListener(v -> acaoDoBotao());
        textos.addView(botao, lp(AUTO, u(52)));

        corpo.addView(textos, lp(0, TODO, 1f));

        LinearLayout.LayoutParams pc = lp(TODO, 0, 1f);
        pc.topMargin = u(12);
        painel.addView(corpo, pc);
        return painel;
    }

    private void acaoDoBotao() {
        if (atual == null) {
            return;
        }
        if (atual.videoId != null) {
            abrirYoutube(atual.videoId);
        } else {
            abrirDetalhes(atual);
        }
    }

    private void abrirYoutube(String videoId) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + videoId));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir o YouTube", Toast.LENGTH_SHORT).show();
        }
    }

    // Tela de detalhes ("Ver mais"): sinopse inteira, ano e nota
    private void abrirDetalhes(Sugestao s) {
        if (overlay != null) {
            return;
        }
        modoFavoritos = false;
        overlay = new FrameLayout(this);
        overlay.setBackground(fundoTela());

        ImageView imagem = new ImageView(this);
        imagem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imagem.setImageBitmap(s.fundo);
        overlay.addView(imagem, new FrameLayout.LayoutParams(TODO, TODO));

        View escurecer = new View(this);
        escurecer.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xF2000000, 0xB3000000, 0x33000000}));
        overlay.addView(escurecer, new FrameLayout.LayoutParams(TODO, TODO));

        LinearLayout textos = vertical();
        textos.setPadding(u(110), u(120), u(60), u(80));
        textos.addView(texto(s.tipo.toUpperCase(Locale.ROOT), 22, SUAVE));

        TextView titulo = texto(s.titulo, 64, BRANCO);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setMaxLines(3);
        titulo.setEllipsize(TextUtils.TruncateAt.END);
        titulo.setPadding(0, u(14), 0, u(10));
        textos.addView(titulo);

        textos.addView(texto(s.detalhe, 26, SUAVE));

        TextView sinopse = texto(s.sinopse, 28, BRANCO);
        sinopse.setMaxLines(9);
        sinopse.setEllipsize(TextUtils.TruncateAt.END);
        sinopse.setPadding(0, u(26), 0, u(34));
        textos.addView(sinopse);

        LinearLayout fechar = horizontal();
        fechar.setGravity(Gravity.CENTER);
        fechar.setPadding(u(46), 0, u(46), 0);
        fechar.setFocusable(true);
        fechar.setBackground(fundoFoco(34, criarVidro(34)));
        fechar.addView(texto("Fechar", 28, BRANCO));
        fechar.setOnClickListener(v -> fecharTodosApps());
        textos.addView(fechar, lp(AUTO, u(68)));

        overlay.addView(textos, new FrameLayout.LayoutParams(u(1000), TODO));
        raiz.addView(overlay, new FrameLayout.LayoutParams(TODO, TODO));
        tela.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        fechar.requestFocus();
    }

    // ---------- Favoritos ----------

    private View criarFaixaFavoritos() {
        HorizontalScrollView faixa = new HorizontalScrollView(this);
        faixa.setHorizontalScrollBarEnabled(false);
        faixa.setClipToPadding(false);
        faixa.setPadding(u(10), u(10), u(10), u(10));

        linhaFavoritos = horizontal();
        linhaFavoritos.setClipChildren(false);
        linhaFavoritos.setGravity(Gravity.CENTER_VERTICAL);
        preencherFavoritos();

        faixa.addView(linhaFavoritos, new FrameLayout.LayoutParams(AUTO, TODO));
        return faixa;
    }

    private void preencherFavoritos() {
        linhaFavoritos.removeAllViews();
        primeiroFoco = null;
        for (final String pacote : new ArrayList<>(favoritos)) {
            ResolveInfo info = acharApp(pacote);
            if (info == null) {
                continue;
            }
            View tile = criarTile(info);
            tile.setOnLongClickListener(v -> {
                favoritos.remove(pacote);
                salvarFavoritos();
                preencherFavoritos();
                if (primeiroFoco != null) {
                    primeiroFoco.requestFocus();
                }
                Toast.makeText(this, "Removido dos favoritos", Toast.LENGTH_SHORT).show();
                return true;
            });
            LinearLayout.LayoutParams p = lp(u(142), u(142));
            p.rightMargin = u(14);
            linhaFavoritos.addView(tile, p);
        }
        LinearLayout mais = criarTileBase();
        mais.addView(texto("+", 54, BRANCO));
        mais.addView(texto("Adicionar", 20, BRANCO));
        mais.setOnClickListener(v -> abrirTodosApps(true));
        linhaFavoritos.addView(mais, lp(u(142), u(142)));
    }

    private LinearLayout criarTileBase() {
        LinearLayout tile = vertical();
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(u(8), u(8), u(8), u(8));
        tile.setBackground(fundoFoco(28, criarVidro(28)));
        tile.setFocusable(true);
        tile.setOnFocusChangeListener((v, tem) -> v.animate()
                .scaleX(tem ? 1.06f : 1f).scaleY(tem ? 1.06f : 1f)
                .setDuration(120).start());
        if (primeiroFoco == null) {
            primeiroFoco = tile;
        }
        return tile;
    }

    // Favorito: só o ícone, grande e sem nome
    private View criarTile(final ResolveInfo info) {
        PackageManager pm = getPackageManager();
        LinearLayout tile = criarTileBase();

        ImageView icone = new ImageView(this);
        icone.setImageDrawable(info.loadIcon(pm));
        icone.setContentDescription(info.loadLabel(pm));
        tile.addView(icone, lp(u(100), u(100)));

        tile.setOnClickListener(v -> abrir(info));
        return tile;
    }

    // ---------- Categorias ----------

    private View criarCategorias() {
        LinearLayout linha = horizontal();
        linha.addView(criarCategoria(I_PLAY, "Filmes e Séries", "Sua diversão sem limites"), pesoCategoria(false));
        linha.addView(criarCategoria(I_NOTA, "Música", "Suas playlists favoritas"), pesoCategoria(false));
        linha.addView(criarCategoria(I_BOLA, "Esportes", "Os melhores jogos"), pesoCategoria(false));
        linha.addView(criarCategoria(I_JOGO, "Jogos", "Diversão garantida"), pesoCategoria(false));
        linha.addView(criarCategoria(I_ENGRENAGEM, "Utilitários", "Mais praticidade"), pesoCategoria(true));
        return linha;
    }

    private LinearLayout.LayoutParams pesoCategoria(boolean ultimo) {
        LinearLayout.LayoutParams p = lp(0, TODO, 1f);
        if (!ultimo) {
            p.rightMargin = u(18);
        }
        return p;
    }

    private View criarCategoria(int tipo, String nome, String detalhe) {
        LinearLayout cartao = horizontal();
        cartao.setGravity(Gravity.CENTER_VERTICAL);
        cartao.setPadding(u(20), u(10), u(18), u(10));
        cartao.setBackground(fundoFoco(28, criarVidro(28)));
        cartao.setFocusable(true);

        FrameLayout bolha = new FrameLayout(this);
        bolha.setBackground(cor(42, 33));
        FrameLayout.LayoutParams pi = new FrameLayout.LayoutParams(u(34), u(34), Gravity.CENTER);
        bolha.addView(new IconeView(this, tipo), pi);
        cartao.addView(bolha, lp(u(66), u(66)));

        LinearLayout textos = vertical();
        textos.setPadding(u(18), 0, u(8), 0);
        TextView t1 = texto(nome, 26, BRANCO);
        t1.setTypeface(null, Typeface.BOLD);
        t1.setMaxLines(1);
        t1.setEllipsize(TextUtils.TruncateAt.END);
        textos.addView(t1);
        TextView t2 = texto(detalhe, 19, SUAVE);
        t2.setMaxLines(2);
        t2.setEllipsize(TextUtils.TruncateAt.END);
        textos.addView(t2);
        cartao.addView(textos, lp(0, AUTO, 1f));

        cartao.addView(new IconeView(this, I_SETA), lp(u(24), u(24)));
        cartao.setOnClickListener(v -> abrirTodosApps(false));
        return cartao;
    }

    // ---------- Barra inferior ----------

    private View criarRodape() {
        LinearLayout barra = horizontal();
        barra.setGravity(Gravity.CENTER_VERTICAL);
        barra.setBackground(criarVidro(29));
        barra.setPadding(u(28), 0, u(34), 0);
        barra.addView(new IconeView(this, I_ESTRELA), lp(u(30), u(30)));
        TextView destaque = texto("Apps em destaque", 24, BRANCO);
        destaque.setPadding(u(16), 0, 0, 0);
        barra.addView(destaque, lp(0, AUTO, 1f));

        barra.addView(new IconeView(this, I_CONTROLE), lp(u(24), u(32)));
        TextView pressione = texto("Pressione", 20, SUAVE);
        pressione.setPadding(u(10), 0, u(14), 0);
        barra.addView(pressione);
        barra.addView(new IconeView(this, I_ANEL), lp(u(28), u(28)));
        TextView abrirMenu = texto("para abrir o menu", 20, SUAVE);
        abrirMenu.setPadding(u(10), 0, 0, 0);
        barra.addView(abrirMenu);
        return barra;
    }

    // ---------- Lista de todos os apps / escolha de favoritos ----------

    private void abrirTodosApps(boolean escolherFavoritos) {
        if (overlay != null) {
            return;
        }
        modoFavoritos = escolherFavoritos;
        overlay = new FrameLayout(this);
        overlay.setBackground(fundoTela());

        LinearLayout caixa = vertical();
        caixa.setPadding(u(96), u(60), u(96), u(40));
        TextView titulo = texto(escolherFavoritos ? "Escolha seus favoritos" : "Todos os aplicativos",
                44, BRANCO);
        titulo.setTypeface(null, Typeface.BOLD);
        caixa.addView(titulo);
        if (escolherFavoritos) {
            caixa.addView(texto("Aperte OK para marcar ou desmarcar. Voltar para concluir.",
                    24, SUAVE));
        }
        final TextView nomeSelecionado = texto(" ", 28, SUAVE);
        nomeSelecionado.setPadding(0, u(10), 0, 0);
        caixa.addView(nomeSelecionado);

        GridView grade = new GridView(this);
        grade.setNumColumns(9);
        grade.setVerticalSpacing(u(14));
        grade.setHorizontalSpacing(u(14));
        grade.setPadding(0, u(20), 0, 0);
        grade.setClipToPadding(false);
        GradientDrawable destaque = new GradientDrawable();
        destaque.setColor(Color.argb(40, 255, 255, 255));
        destaque.setStroke(u(5), AZUL);
        destaque.setCornerRadius(u(28));
        grade.setSelector(destaque);
        grade.setDrawSelectorOnTop(true);
        grade.setAdapter(new AppsAdapter());
        grade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> pai, View v, int posicao, long id) {
                nomeSelecionado.setText(apps.get(posicao).loadLabel(getPackageManager()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> pai) {
                nomeSelecionado.setText(" ");
            }
        });
        grade.setOnItemClickListener((pai, v, posicao, id) -> {
            ResolveInfo info = apps.get(posicao);
            if (modoFavoritos) {
                alternarFavorito(info.activityInfo.packageName);
                ((BaseAdapter) pai.getAdapter()).notifyDataSetChanged();
            } else {
                abrir(info);
            }
        });
        caixa.addView(grade, lp(TODO, 0, 1f));

        overlay.addView(caixa, new FrameLayout.LayoutParams(TODO, TODO));
        raiz.addView(overlay, new FrameLayout.LayoutParams(TODO, TODO));
        tela.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        grade.requestFocus();
    }

    private void fecharTodosApps() {
        if (overlay == null) {
            return;
        }
        raiz.removeView(overlay);
        overlay = null;
        tela.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        if (modoFavoritos) {
            modoFavoritos = false;
            salvarFavoritos();
            preencherFavoritos();
        }
        if (primeiroFoco != null) {
            primeiroFoco.requestFocus();
        }
    }

    // Lista de todos os apps: só os ícones
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
            LinearLayout linha;
            ImageView icone;
            if (convertView == null) {
                linha = vertical();
                linha.setGravity(Gravity.CENTER);
                linha.setPadding(u(16), u(18), u(16), u(18));

                icone = new ImageView(MainActivity.this);
                icone.setId(1);
                linha.addView(icone, lp(u(120), u(120)));
            } else {
                linha = (LinearLayout) convertView;
                icone = linha.findViewById(1);
            }
            PackageManager pm = getPackageManager();
            ResolveInfo info = apps.get(i);
            boolean marcado = modoFavoritos
                    && favoritos.contains(info.activityInfo.packageName);
            linha.setBackground(cor(marcado ? 70 : 0, 28));
            icone.setImageDrawable(info.loadIcon(pm));
            icone.setContentDescription(info.loadLabel(pm));
            return linha;
        }
    }

    // ---------- Favoritos: dados ----------

    private String rotulo(ResolveInfo info) {
        return info.loadLabel(getPackageManager()).toString().toLowerCase(Locale.ROOT).trim();
    }

    private ResolveInfo acharPorNome(String chave) {
        for (ResolveInfo info : apps) {
            if (rotulo(info).equals(chave)) {
                return info;
            }
        }
        for (ResolveInfo info : apps) {
            if (rotulo(info).contains(chave)) {
                return info;
            }
        }
        return null;
    }

    private void carregarFavoritos() {
        prefs = getSharedPreferences("launcher", MODE_PRIVATE);
        String salvo = prefs.getString("favoritos", null);
        favoritos.clear();
        if (salvo == null) {
            for (String chave : FAVORITOS_PADRAO) {
                ResolveInfo achado = acharPorNome(chave);
                if (achado != null
                        && !favoritos.contains(achado.activityInfo.packageName)) {
                    favoritos.add(achado.activityInfo.packageName);
                }
            }
            if (favoritos.isEmpty()) {
                for (int i = 0; i < Math.min(8, apps.size()); i++) {
                    favoritos.add(apps.get(i).activityInfo.packageName);
                }
            }
        } else if (!salvo.isEmpty()) {
            for (String pacote : salvo.split(",")) {
                if (acharApp(pacote) != null) {
                    favoritos.add(pacote);
                }
            }
        }
    }

    private void salvarFavoritos() {
        prefs.edit().putString("favoritos", TextUtils.join(",", favoritos)).apply();
    }

    private void alternarFavorito(String pacote) {
        if (favoritos.contains(pacote)) {
            favoritos.remove(pacote);
        } else {
            favoritos.add(pacote);
        }
    }

    private ResolveInfo acharApp(String pacote) {
        for (ResolveInfo info : apps) {
            if (info.activityInfo.packageName.equals(pacote)) {
                return info;
            }
        }
        return null;
    }

    // ---------- Sugestões (TMDB e YouTube) ----------

    private void mostrar(final Sugestao s) {
        atual = s;
        imagemCartaz.animate().alpha(0f).setDuration(250).withEndAction(() -> {
            imagemCartaz.setImageBitmap(s.cartaz != null ? s.cartaz : s.fundo);
            tituloSug.setText(s.titulo);
            detalheSug.setText(s.detalhe);
            sinopseSug.setText(s.sinopse);
            rotuloBotao.setText(s.videoId != null ? "Assistir" : "Ver mais");
            imagemCartaz.animate().alpha(1f).setDuration(350).start();
        }).start();
    }

    private void adicionar(Sugestao s) {
        sugestoes.add(s);
        if (sugestoes.size() == 1) {
            indice = 0;
            mostrar(s);
            handler.removeCallbacks(girar);
            handler.postDelayed(girar, SEGUNDOS_POR_SUGESTAO * 1000L);
        }
    }

    private void avisarSemSugestoes(final String mensagem) {
        runOnUiThread(() -> {
            if (sugestoes.isEmpty()) {
                tituloSug.setText(mensagem);
            }
        });
    }

    private void carregarSugestoes() {
        boolean achouFilmes = carregarFilmes();
        boolean achouVideos = carregarVideos();
        if (!achouFilmes && !achouVideos) {
            if (BuildConfig.TMDB_TOKEN.isEmpty() && BuildConfig.YOUTUBE_KEY.isEmpty()) {
                avisarSemSugestoes("Sem chaves de sugestões");
            } else {
                avisarSemSugestoes("Sem sugestões agora");
            }
        }
    }

    private boolean carregarFilmes() {
        if (BuildConfig.TMDB_TOKEN.isEmpty()) {
            return false;
        }
        boolean achou = false;
        try {
            URL url = new URL("https://api.themoviedb.org/3/trending/all/week?language=pt-BR");
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
            conexao.setRequestProperty("Authorization", "Bearer " + BuildConfig.TMDB_TOKEN);
            conexao.setRequestProperty("accept", "application/json");
            conexao.setConnectTimeout(8000);
            conexao.setReadTimeout(8000);
            if (conexao.getResponseCode() != 200) {
                return false;
            }
            JSONArray lista = new JSONObject(lerTudo(conexao.getInputStream()))
                    .getJSONArray("results");

            int encontrados = 0;
            for (int i = 0; i < lista.length() && encontrados < 10; i++) {
                JSONObject o = lista.getJSONObject(i);
                if (o.isNull("backdrop_path") || o.isNull("poster_path")) {
                    continue;
                }
                String media = o.optString("media_type", "");
                boolean serie = media.equals("tv");
                String titulo = (o.has("title") && !o.isNull("title"))
                        ? o.getString("title") : o.optString("name", "");
                String sinopse = o.isNull("overview") ? "" : o.optString("overview", "");
                String data = o.optString(serie ? "first_air_date" : "release_date", "");
                String ano = data.length() >= 4 ? data.substring(0, 4) : "";
                double nota = o.optDouble("vote_average", 0);
                StringBuilder detalhe = new StringBuilder(ano);
                if (nota > 0) {
                    if (detalhe.length() > 0) {
                        detalhe.append("  •  ");
                    }
                    detalhe.append("Nota ")
                            .append(String.format(Locale.US, "%.1f", nota).replace('.', ','));
                }
                Bitmap fundo = baixarImagem(
                        "https://image.tmdb.org/t/p/w780" + o.getString("backdrop_path"));
                Bitmap cartaz = baixarImagem(
                        "https://image.tmdb.org/t/p/w342" + o.getString("poster_path"));
                if (fundo == null || cartaz == null) {
                    continue;
                }
                encontrados++;
                achou = true;
                final Sugestao s = new Sugestao(serie ? "Série" : "Filme", titulo, sinopse,
                        detalhe.toString(), null, fundo, cartaz);
                runOnUiThread(() -> adicionar(s));
            }
        } catch (Exception e) {
            return achou;
        }
        return achou;
    }

    private boolean carregarVideos() {
        if (BuildConfig.YOUTUBE_KEY.isEmpty()) {
            return false;
        }
        boolean achou = false;
        try {
            URL url = new URL("https://www.googleapis.com/youtube/v3/videos"
                    + "?part=snippet&chart=mostPopular&regionCode=BR&maxResults=8&key="
                    + BuildConfig.YOUTUBE_KEY);
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
            conexao.setConnectTimeout(8000);
            conexao.setReadTimeout(8000);
            if (conexao.getResponseCode() != 200) {
                return false;
            }
            JSONArray itens = new JSONObject(lerTudo(conexao.getInputStream()))
                    .getJSONArray("items");

            int total = 0;
            for (int i = 0; i < itens.length() && total < 5; i++) {
                JSONObject item = itens.getJSONObject(i);
                String id = item.getString("id");
                JSONObject snippet = item.getJSONObject("snippet");
                JSONObject miniaturas = snippet.getJSONObject("thumbnails");
                String endereco = null;
                String[] tamanhos = {"maxres", "standard", "high"};
                for (String tamanho : tamanhos) {
                    if (miniaturas.has(tamanho)) {
                        endereco = miniaturas.getJSONObject(tamanho).getString("url");
                        break;
                    }
                }
                if (endereco == null) {
                    continue;
                }
                Bitmap fundo = baixarImagem(endereco);
                if (fundo == null) {
                    continue;
                }
                String canal = snippet.optString("channelTitle", "");
                String descricao = snippet.optString("description", "").replace('\n', ' ');
                if (descricao.trim().isEmpty()) {
                    descricao = canal;
                }
                total++;
                achou = true;
                final Sugestao s = new Sugestao("Vídeo", snippet.getString("title"),
                        descricao, canal, id, fundo, null);
                runOnUiThread(() -> adicionar(s));
            }
        } catch (Exception e) {
            return achou;
        }
        return achou;
    }

    private String lerTudo(InputStream entrada) throws Exception {
        BufferedReader leitor = new BufferedReader(new InputStreamReader(entrada, "UTF-8"));
        StringBuilder texto = new StringBuilder();
        String linha;
        while ((linha = leitor.readLine()) != null) {
            texto.append(linha);
        }
        leitor.close();
        return texto.toString();
    }

    private Bitmap baixarImagem(String endereco) throws Exception {
        HttpURLConnection conexao = (HttpURLConnection) new URL(endereco).openConnection();
        conexao.setConnectTimeout(8000);
        conexao.setReadTimeout(8000);
        InputStream entrada = conexao.getInputStream();
        Bitmap imagem = BitmapFactory.decodeStream(entrada);
        entrada.close();
        return imagem;
    }

    // ---------- Ferramentas de montagem ----------

    private TextView secao(String titulo) {
        TextView t = texto(titulo, 32, BRANCO);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private TextView texto(String conteudo, float px, int corTexto) {
        TextView t = new TextView(this);
        t.setText(conteudo);
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, u(px));
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

    private Drawable cor(int alfa, int raioU) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.argb(alfa, 255, 255, 255));
        d.setCornerRadius(u(raioU));
        return d;
    }

    // Item selecionado: anel azul claro com brilho, como o Netflix da imagem
    private Drawable fundoFoco(int raioU, Drawable normal) {
        GradientDrawable halo = new GradientDrawable();
        halo.setColor(Color.TRANSPARENT);
        halo.setStroke(u(12), Color.argb(70, 120, 175, 255));
        halo.setCornerRadius(u(raioU));

        GradientDrawable anel = new GradientDrawable();
        anel.setColor(Color.argb(40, 255, 255, 255));
        anel.setStroke(u(4), AZUL);
        anel.setCornerRadius(u(raioU));

        Drawable comVidro = new LayerDrawable(new Drawable[]{criarVidro(raioU), halo, anel});
        StateListDrawable estados = new StateListDrawable();
        estados.addState(new int[]{android.R.attr.state_focused}, comVidro);
        estados.addState(new int[]{}, normal);
        return estados;
    }

    private Drawable criarVidro(int raioU) {
        return new Vidro(raioU);
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
