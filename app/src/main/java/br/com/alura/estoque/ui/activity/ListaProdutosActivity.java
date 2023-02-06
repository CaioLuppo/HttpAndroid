package br.com.alura.estoque.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.alura.estoque.R;
import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.repository.ProdutoRepository;
import br.com.alura.estoque.ui.dialog.EditaProdutoDialog;
import br.com.alura.estoque.ui.dialog.SalvaProdutoDialog;
import br.com.alura.estoque.ui.recyclerview.adapter.ListaProdutosAdapter;

public class ListaProdutosActivity extends AppCompatActivity {

    private static final String TITULO_APPBAR = "Lista de produtos";
    private ListaProdutosAdapter adapter;
    private ProdutoRepository produtoRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);
        setTitle(TITULO_APPBAR);

        configuraListaProdutos();
        configuraFabSalvaProduto();

        produtoRepository = new ProdutoRepository(this);
        buscaProdutos();
    }

    private void buscaProdutos() {
        produtoRepository.buscaProdutos(new ProdutoRepository.DadosCarregadosCallback<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> resultado) {
                adapter.atualiza(resultado);
            }

            @Override
            public void quandoFalha(String erro) {
                mostraErro("Não foi possível carregar produtos novos");
            }
        });
    }

    private void mostraErro(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void configuraListaProdutos() {
        RecyclerView listaProdutos = findViewById(R.id.activity_lista_produtos_lista);
        adapter = new ListaProdutosAdapter(this, this::abreFormularioEditaProduto);
        listaProdutos.setAdapter(adapter);
        adapter.setOnItemClickRemoveContextMenuListener(
                ((posicao, produtoEscolhido) -> {
                    produtoRepository.remove(
                            posicao,
                            produtoEscolhido,
                            new ProdutoRepository.DadosCarregadosCallback<Void>() {
                                @Override
                                public void quandoSucesso(Void resultado) {
                                    adapter.remove(posicao);
                                }

                                @Override
                                public void quandoFalha(String erro) {
                                    mostraErro("Não foi possível remover");
                                }
                            }
                    );

                })
        );
    }

    private void configuraFabSalvaProduto() {
        FloatingActionButton fabAdicionaProduto = findViewById(R.id.activity_lista_produtos_fab_adiciona_produto);
        fabAdicionaProduto.setOnClickListener(v -> abreFormularioSalvaProduto());
    }

    private void abreFormularioSalvaProduto() {
        new SalvaProdutoDialog(
                this,
                produtoCriado -> produtoRepository.salva(produtoCriado, new ProdutoRepository.DadosCarregadosCallback<Produto>() {
                    @Override
                    public void quandoSucesso(Produto produto) {
                        adapter.adiciona(produto);
                    }

                    @Override
                    public void quandoFalha(String erro) {
                        mostraErro(erro);
                    }
                })
        ).mostra();
    }

    private void abreFormularioEditaProduto(int posicao, Produto produto) {
        new EditaProdutoDialog(this, produto,
                produtoEditado -> produtoRepository.edita(
                        produtoEditado,
                        new ProdutoRepository.DadosCarregadosCallback<Produto>() {
                            @Override
                            public void quandoSucesso(Produto resultado) {
                                adapter.edita(posicao, resultado);
                            }

                            @Override
                            public void quandoFalha(String erro) {
                                mostraErro("Não foi possível editar");
                            }
                        }
                ))
                .mostra();
    }


}
