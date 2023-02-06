package br.com.alura.estoque.retrofit;

import androidx.annotation.NonNull;

import br.com.alura.estoque.retrofit.service.ProdutoService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EstoqueRetrofit {

    public static final String BASE_URL = "http://localhost:8080/";
    private final ProdutoService produtoService;
    private final OkHttpClient client;

    public EstoqueRetrofit() {
        client = getLogClient();
        produtoService = getService();
    }

    @NonNull
    private OkHttpClient getLogClient() {
        final OkHttpClient client;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        return client;
    }

    @NonNull
    private ProdutoService getService() {
        final ProdutoService produtoService;
        // Instancia retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        produtoService = retrofit.create(ProdutoService.class);
        return produtoService;
    }

    public ProdutoService getProdutoService() {
        return produtoService;
    }

}

