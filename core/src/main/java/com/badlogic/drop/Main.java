package com.badlogic.drop;

import static com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable.draw;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    // variáveis do tutorial
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;

    //rendering
    SpriteBatch spriteBatch;
    FitViewport viewport;

    //pacos para input
    Sprite bucketSprite;

    Vector2 touchPos;

    //pacos para a logica
    Array<Sprite> dropSprites;

    float dropTimer; // deixar a criacao de gotas mais lenta

    //deteccao de colisao das gotas com o balde para isso utilizar rentangulos
    Rectangle bucketRectangle;
    Rectangle dropRectangle;

    @Override
    public void create() {
        //tutorial images
        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");

        // tutorial sounds
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        //rendering
        spriteBatch = new SpriteBatch();
        /*O viewport controla como vemos o jogo. É como uma janela do nosso mundo para outro: o mundo do jogo.
        A viewport controla o tamanho dessa “janela” e como ela é colocada na nossa tela.*/
        viewport = new FitViewport(5, 8);

        //pacos para input
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1, 1);

        touchPos = new Vector2();

        //pacos para a logica
        dropSprites = new Array<>();

        //detecacao de colisao entre o balde e a gota
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        //implementacao do som e musica
        music.setLooping(true); //adiciona um loop para a musica de fundo
        music.setVolume(.5f); // altera o volume do som.  Volume é um valor de 0f a 1f, sendo 1f o volume normal do audio.
        music.play();




    }

    @Override
    public void resize(int width, int height) {
        // Resize your application here. The parameters represent the new window size.

        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        // Draw your application here.
        input();
        logic();
        draw();
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY()); // Veja onde o toque aconteceu na tela
            viewport.unproject(touchPos); // Converter as unidades para as unidades mundiais da janela de visualização
            bucketSprite.setCenterX(touchPos.x); // Alterar a posição centralizada horizontalmente do balde
        }
    }

    private void logic() {
        // Armazene worldWidth e worldHeight como variáveis ​​locais para brevidade
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // Fixe x em valores entre 0 e worldWidth
        //bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth));

        // Armazene o tamanho do balde para maior brevidade
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        // Subtraia a largura do balde
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime(); // recuperar o delta atual

        /*
        // esse loop fará com que o jogo feche se ficar muito tempo aberto
        for (Sprite dropSprite : dropSprites) {
            dropSprite.translateY(-2f * delta); //mova a gota para baixo a cada quadro
        }*/

        // Aplique a posição e o tamanho do bucket ao bucketRectangle
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-2f * delta);
            // Aplique a posição e o tamanho da gota ao dropRectangle
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i);
            else if (bucketRectangle.overlaps(dropRectangle)) { // Verifique se o balde sobrepõe a gota
                dropSprites.removeIndex(i); // Remova a gota

                // implementacao dos sons
                dropSound.play(); // toca o som de gota
            }
        }

        //createDroplet();

        dropTimer += delta; // Adiciona o delta atual ao temporizador
        if (dropTimer > 1f) { // Verifique se já passou mais de um segundo
            dropTimer = 0; // Reiniciar o temporizador
            createDroplet(); // Crie a gota
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK); //Limpa a tela. É uma boa prática limpar a tela a cada quadro.

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined); /*mostra como o Viewport é aplicado ao SpriteBatch.
        Isso é necessário para que as imagens sejam mostradas no lugar correto.*/
        spriteBatch.begin();


        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        //Reorganizar a sequencia
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight); // "desenha" o background
        //spriteBatch.draw(bucketTexture, 0, 0, 1, 1); // "desenha" o balde

        //pacos para o input
        bucketSprite.draw(spriteBatch);

        //pacos para a logica

        // instancie cada sprite
        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }


        spriteBatch.end();

    }

    private void createDroplet() {
        // crie variáveis locais para conveniência
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // crie o drop sprite
        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        //dropSprite.setX(0);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth)); // Randomize a posicao x das gotas
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite); // adicione a lista
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy application's resources here.

    }
}
