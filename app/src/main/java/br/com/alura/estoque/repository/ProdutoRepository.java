package br.com.alura.estoque.repository;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.callback.BaseCallback;
import br.com.alura.estoque.retrofit.callback.CallbackSemRetorno;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class ProdutoRepository {


    private final ProdutoDAO dao;
    private final ProdutoService produtoService;

    public ProdutoRepository(Context context) {
        EstoqueDatabase db = EstoqueDatabase.getInstance(context);
        this.dao = db.getProdutoDAO();
        produtoService = new EstoqueRetrofit().getProdutoService();
    }


    // Métodos publicos

    public void buscaProdutos(DadosCarregadosCallback<List<Produto>> call) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    call.quandoSucesso(resultado);
                    buscaProdutosWeb(call);
                })
                .execute();
    }

    public void salva(Produto produto, DadosCarregadosCallback<Produto> callback) {
        salvaNaApi(produto, callback);
    }

    public void edita(Produto produto, DadosCarregadosCallback<Produto> callback) {

        Call<Produto> call = produtoService.edita(produto.getId(), produto);
        call.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto resultado) {
                new BaseAsyncTask<>(() -> {
                    dao.atualiza(produto);
                    return produto;
                }, callback::quandoSucesso)
                        .execute();
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));

    }

    public void remove(int posicao, Produto produto
                       ,DadosCarregadosCallback<Void> callback) {

        removeNaApi(produto, callback);


    }

    private void removeNaApi(Produto produto, DadosCarregadosCallback<Void> callback) {
        Call<Void> call = produtoService.remove(produto.getId());
        call.enqueue(new CallbackSemRetorno(new CallbackSemRetorno.RespostaCallback() {
            @Override
            public void quandoSucesso() {
                removeInterno(produto, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

    private void removeInterno(Produto produto, DadosCarregadosCallback<Void> callback) {
        new BaseAsyncTask<>(() -> {
            dao.remove(produto);
            return null;
        }, callback::quandoSucesso)
                .execute();
    }


    // Métodos privados

    private void buscaProdutosWeb(DadosCarregadosCallback<List<Produto>> callback) {
        Call<List<Produto>> callTodos = produtoService.buscaTodos();
        callTodos.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> resultado) {
                atualizaInterno(resultado, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));

    }

    private void atualizaInterno(List<Produto> produtosNovos, DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(
                () -> {
                    dao.salva(produtosNovos);
                    return dao.buscaTodos();
                },
                callback::quandoSucesso
        ).execute();
    }

    private void salvaNaApi(Produto produto, DadosCarregadosCallback<Produto> callback) {
        Call<Produto> salva = produtoService.salva(produto);
        salva.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<Produto>(

        ) {
            @Override
            public void quandoSucesso(Produto resultado) {
                salvaInterno(resultado, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

    private void salvaInterno(Produto produtoSalvo, DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produtoSalvo);
            return dao.buscaProduto(id);
        }, callback::quandoSucesso)
                .execute();
    }


    // Interface

    public interface DadosCarregadosCallback <T> {
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }

}
