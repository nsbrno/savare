package com.savare.activity.material.designer;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.github.clans.fab.FloatingActionButton;
import com.savare.R;
import com.savare.activity.fragment.ClienteCadastroFragment;
import com.savare.activity.fragment.OrcamentoFragment;
import com.savare.adapter.ItemUniversalAdapter;
import com.savare.adapter.PessoaAdapter;
import com.savare.beans.CidadeBeans;
import com.savare.beans.PessoaBeans;
import com.savare.funcoes.FuncoesPersonalizadas;
import com.savare.funcoes.rotinas.OrcamentoRotinas;
import com.savare.funcoes.rotinas.PessoaRotinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno Nogueira Silva on 02/02/2016.
 */
public class ClienteListaMDActivity extends AppCompatActivity {

    private ListView listViewPessoa;
    private FloatingActionButton itemMenuNovoCliente;
    private Spinner spinnerListaCidade;
    private ProgressBar progressBarStatus;
    private List<PessoaBeans> listaPessoas;
    private String telaChamou,
            idOrcamento;
    private Toolbar toolbarCabecalho;
    private boolean pesquisando = false;
    private int positionSpinnerListaCidade = 0,
                positionScrollListViewPessoa = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_lista_md);

        recuperaCampo();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle intentParametro = getIntent().getExtras();
        if (intentParametro != null) {

            this.telaChamou = intentParametro.getString(ListaOrcamentoPedidoMDActivity.KEY_TELA_CHAMADA);

            if (telaChamou.equals(OrcamentoFragment.KEY_TELA_ORCAMENTO_FRAGMENTO)) {
                idOrcamento = intentParametro.getString(OrcamentoFragment.KEY_ID_ORCAMENTO);
            }
        }

        itemMenuNovoCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre a tela inicial do sistema
                Intent intentNovo = new Intent(ClienteListaMDActivity.this, ClienteCadastroFragment.class);
                startActivity(intentNovo);
            }
        });

        spinnerListaCidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                CidadeBeans cidadeBeans = (CidadeBeans) parent.getSelectedItem();
                // Checa se tem alguma lista de cidade
                if (!cidadeBeans.getDescricao().contains("Nenhum valor encontrado")) {

                    LoaderPessoa carregarListaPessoa = new LoaderPessoa(ClienteListaMDActivity.this, cidadeBeans, null);
                    carregarListaPessoa.execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listViewPessoa.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if((telaChamou != null) && (telaChamou.equals(ListaOrcamentoPedidoMDActivity.KEY_TELA_LISTA_ORCAMENTO_PEDIDO))){

                    PessoaBeans pessoa = new PessoaBeans();
                    pessoa = (PessoaBeans) listViewPessoa.getAdapter().getItem(position);

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClienteListaMDActivity.this);

                    // Cria uma intent para returnar um valor para activity ProdutoLista
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("ID_CFACLIFO", String.valueOf(pessoa.getIdPessoa()));
                    returnIntent.putExtra("ID_CFAESTAD", String.valueOf(pessoa.getEstadoPessoa().getCodigoEstado()));
                    returnIntent.putExtra("ID_CFACIDAD", String.valueOf(pessoa.getCidadePessoa().getIdCidade()));
                    returnIntent.putExtra("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
                    returnIntent.putExtra("PESSOA_CLIENTE", String.valueOf(pessoa.getPessoa()));
                    returnIntent.putExtra("NOME_CLIENTE", pessoa.getNomeRazao());
                    returnIntent.putExtra("IE_RG_CLIENTE", pessoa.getIeRg());
                    returnIntent.putExtra("CPF_CGC_CLIENTE", pessoa.getCpfCnpj());
                    returnIntent.putExtra("CODIGO_CLI", String.valueOf(pessoa.getCodigoCliente()));
                    returnIntent.putExtra("CODIGO_USU", String.valueOf(pessoa.getCodigoUsuario()));
                    returnIntent.putExtra("CODIGO_TRA", String.valueOf(pessoa.getCodigoTransportadora()));
                    returnIntent.putExtra("CODIGO_FUN", String.valueOf(pessoa.getCodigoFuncionario()));
                    returnIntent.putExtra("ENDERECO_CLIENTE", pessoa.getEnderecoPessoa().getLogradouro() + ", " + pessoa.getEnderecoPessoa().getNumero());
                    returnIntent.putExtra("BAIRRO_CLIENTE", pessoa.getEnderecoPessoa().getBairro());
                    returnIntent.putExtra("CEP_CLIENTE", pessoa.getEnderecoPessoa().getCep());
                    if (pessoa.isCadastroNovo()){
                        returnIntent.putExtra("CADASTRO_NOVO", "S");
                    }
                    setResult(ListaOrcamentoPedidoMDActivity.RETORNA_CLIENTE, returnIntent);
                    // Fecha a tela de detalhes de produto
                    finish();

                } else if((telaChamou != null) && (telaChamou.equals(OrcamentoFragment.KEY_TELA_ORCAMENTO_FRAGMENTO))){

                    PessoaBeans pessoa = new PessoaBeans();
                    pessoa = (PessoaBeans) listViewPessoa.getAdapter().getItem(position);

                    FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(ClienteListaMDActivity.this);
                    // Preenche o ContentValues com os dados da pessoa
                    ContentValues dadosCliente = new ContentValues();
                    dadosCliente.put("ID_CFACLIFO", pessoa.getIdPessoa());
                    dadosCliente.put("ID_CFAESTAD", pessoa.getEstadoPessoa().getCodigoEstado());
                    dadosCliente.put("ID_CFACIDAD", pessoa.getCidadePessoa().getIdCidade());
                    dadosCliente.put("ID_SMAEMPRE", funcoes.getValorXml("CodigoEmpresa"));
                    dadosCliente.put("PESSOA_CLIENTE", String.valueOf(pessoa.getPessoa()));
                    dadosCliente.put("NOME_CLIENTE", pessoa.getNomeRazao());
                    dadosCliente.put("IE_RG_CLIENTE", pessoa.getIeRg());
                    dadosCliente.put("CPF_CGC_CLIENTE", pessoa.getCpfCnpj());
                    dadosCliente.put("ENDERECO_CLIENTE", pessoa.getEnderecoPessoa().getLogradouro() + ", " + pessoa.getEnderecoPessoa().getNumero());
                    dadosCliente.put("BAIRRO_CLIENTE", pessoa.getEnderecoPessoa().getBairro());
                    dadosCliente.put("CEP_CLIENTE", pessoa.getEnderecoPessoa().getCep());
                    if (pessoa.isCadastroNovo()){
                        dadosCliente.put("CADASTRO_NOVO", "S");
                    }
                    OrcamentoRotinas orcamentoRotinas = new OrcamentoRotinas(ClienteListaMDActivity.this);
                    // Atualiza o cliente do orcamento
                    int qtdAlterado = orcamentoRotinas.updateOrcamento(dadosCliente, idOrcamento);

                    // Checa se atualizou algum orcamento
                    if(qtdAlterado > 0){

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("NOME_CLIENTE", pessoa.getNomeRazao());
                        returnIntent.putExtra("ID_CFACLIFO", String.valueOf(pessoa.getIdPessoa()));
                        returnIntent.putExtra("CODIGO_CLI", String.valueOf(pessoa.getCodigoCliente()));
                        returnIntent.putExtra("CODIGO_USU", String.valueOf(pessoa.getCodigoUsuario()));
                        returnIntent.putExtra("CODIGO_TRA", String.valueOf(pessoa.getCodigoTransportadora()));
                        returnIntent.putExtra("CODIGO_FUN", String.valueOf(pessoa.getCodigoFuncionario()));

                        setResult(OrcamentoFragment.RETORNA_CLIENTE, returnIntent);
                        // Fecha a tela de detalhes de produto
                        finish();

                    } else {
                        setResult(OrcamentoFragment.ERRO_RETORNA_CLIENTE);
                        // Fecha a tela de detalhes de produto
                        finish();
                    }

                } else {
                    //Pega os dados da pessoa que foi clicado
                    PessoaBeans pessoa = (PessoaBeans) parent.getItemAtPosition(position);

                    // Abre a tela inicial do sistema
                    Intent intent = new Intent(ClienteListaMDActivity.this, ClienteDetalhesMDActivity.class);
                    intent.putExtra("ID_CFACLIFO", String.valueOf(pessoa.getIdPessoa()));
                    intent.putExtra("CODIGO_CLI", String.valueOf(pessoa.getCodigoCliente()));
                    intent.putExtra("CODIGO_USU", String.valueOf(pessoa.getCodigoUsuario()));
                    intent.putExtra("CODIGO_TRA", String.valueOf(pessoa.getCodigoTransportadora()));
                    intent.putExtra("CODIGO_FUN", String.valueOf(pessoa.getCodigoFuncionario()));
                    if (pessoa.isCadastroNovo()){
                        intent.putExtra("CADASTRO_NOVO", "S");
                    }
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        carregarListaCidades();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt(KEY_POSITION_SPINNER_LISTA_CIDADE, spinnerListaCidade.getSelectedItemPosition());
        positionSpinnerListaCidade = spinnerListaCidade.getSelectedItemPosition();
        positionScrollListViewPessoa = listViewPessoa.getFirstVisiblePosition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cliente_lista_md, menu);

        // Configuração associando item de pesquisa com a SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) menu.findItem(R.id.menu_cliente_lista_md_search_pesquisar).getActionView();
        searchView.setQueryHint("Pesquisar");

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            /**
             * Botao para submeter a pesquisa.
             * So eh executado quando clicado no botao.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!pesquisando) {

                    pesquisando = true;

                    String where = "CFACLIFO.NOME_RAZAO LIKE '%" + query + "%' OR "
                            + "CFACLIFO.NOME_FANTASIA LIKE '%" + query + "%' OR "
                            + "CFACLIFO.CPF_CNPJ LIKE '%" + query + "%' OR "
                            + "CFACIDAD.DESCRICAO LIKE '%" + query + "%' OR "
                            + "CFAENDER.BAIRRO LIKE '%" + query + "%' OR "
                            + "CFASTATU.DESCRICAO LIKE '%" + query + "%' ";

                    //PessoaRotinas pessoaRotinas = new PessoaRotinas(ClienteListaMDActivity.this);

                    CidadeBeans cidade = (CidadeBeans) spinnerListaCidade.getSelectedItem();

                    LoaderPessoa carregaListaPessoa = new LoaderPessoa(ClienteListaMDActivity.this, cidade, where);
                    carregaListaPessoa.execute();
                }
                return false;
            } // Fim do onQueryTextSubmit

            /**
             * Pega todo o texto digitado
             */
            @Override
            public boolean onQueryTextChange(String newText) {


                return false;
            } // Fim do onQueryTextChange


            //OnQueryTextListener

        }); // Fim do setOnQueryTextListener


        return super.onCreateOptionsMenu(menu);
    } // Fim do onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void carregarListaCidades() {
        List<CidadeBeans> listaCidade = new ArrayList<CidadeBeans>();

        PessoaRotinas pessoaRotinas = new PessoaRotinas(ClienteListaMDActivity.this);

        listaCidade = pessoaRotinas.listaCidadeCliente(PessoaRotinas.KEY_TIPO_CLIENTE);

        if ((listaCidade != null) && (listaCidade.size() > 0)) {

            ItemUniversalAdapter adapterListaCidade = new ItemUniversalAdapter(ClienteListaMDActivity.this, ItemUniversalAdapter.CIDADE_DARK);
            adapterListaCidade.setListaCidade(listaCidade);

            spinnerListaCidade.setAdapter(adapterListaCidade);
        }
        spinnerListaCidade.setSelection(positionSpinnerListaCidade);
    }

    private void recuperaCampo() {
        listViewPessoa = (ListView) findViewById(R.id.activity_cliente_lista_md_list_pessoa);
        itemMenuNovoCliente = (FloatingActionButton) findViewById(R.id.activity_cliente_lista_md_novo_cliente);
        spinnerListaCidade = (Spinner) findViewById(R.id.activity_cliente_lista_md_spinner_cidades);
        progressBarStatus = (ProgressBar) findViewById(R.id.activity_cliente_lista_md_progressBar_status);

        toolbarCabecalho = (Toolbar) findViewById(R.id.activity_cliente_lista_md_toolbar_cabecalho);
        // Adiciona uma titulo para toolbar
        toolbarCabecalho.setTitle(this.getResources().getString(R.string.app_name));
        toolbarCabecalho.setTitleTextColor(getResources().getColor(R.color.branco));
        //toolbarInicio.setLogo(R.mipmap.ic_launcher);
        // Seta uma toolBar para esta activiy(tela)
        setSupportActionBar(toolbarCabecalho);
    }


    public class LoaderPessoa extends AsyncTask<Void, Void, Void> {

        private Context context;
        private CidadeBeans cidade;
        private String where;
        PessoaAdapter adapterPessoa;

        public LoaderPessoa(Context context, CidadeBeans cidade, String where) {
            this.context = context;
            this.cidade = cidade;
            this.where = where;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBarStatus.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Cria a variavel para montar a clausula where do sql
                String whereAux = null;

                PessoaRotinas pessoaRotinas = new PessoaRotinas(context);

                if ((!cidade.getDescricao().equalsIgnoreCase("Nenhum valor encontrado")) &&
                        (!cidade.getDescricao().equalsIgnoreCase("Todas as Cidades")) &&
                        (!cidade.getDescricao().equalsIgnoreCase(getResources().getString(R.string.sem_cidade)))) {

                    // Monta a clausula where do sql
                    whereAux = "( CFACIDAD.DESCRICAO LIKE '%" + cidade.getDescricao().replace("'", "%") + "%' )";

                    if ((where != null) && (where.length() > 1)){
                        whereAux = where;
                    }

                    // Cria a lista com as pessoas de acordo com a cidade selecionada
                    listaPessoas = pessoaRotinas.listaPessoaResumido(whereAux, PessoaRotinas.KEY_TIPO_CLIENTE, progressBarStatus);

                } else if (cidade.getDescricao().equalsIgnoreCase(getResources().getString(R.string.sem_cidade))){
                    whereAux = "( (CFACIDAD.DESCRICAO IS NULL) OR (CFACIDAD.DESCRICAO = '') )";

                    if ((where != null) && (where.length() > 1)){
                        whereAux = where;
                    }

                    // Cria a lista com as pessoas de acordo com a cidade selecionada
                    listaPessoas = pessoaRotinas.listaPessoaResumido(whereAux, PessoaRotinas.KEY_TIPO_CLIENTE, progressBarStatus);
                } else if (cidade.getDescricao().equalsIgnoreCase("Todas as Cidades")) {

                    if ((where != null) && (where.length() > 1)){
                        // Preenche a lista de pessoas
                        listaPessoas = pessoaRotinas.listaPessoaResumido(where, PessoaRotinas.KEY_TIPO_CLIENTE, progressBarStatus);
                    } else {
                        // Preenche a lista de pessoas
                        listaPessoas = pessoaRotinas.listaPessoaResumido(null, PessoaRotinas.KEY_TIPO_CLIENTE, progressBarStatus);
                    }
                }
                if ( (listaPessoas != null) && (listaPessoas.size() > 0) ) {
                    // Seta o adapter com a nova lista
                    adapterPessoa = new PessoaAdapter(context, listaPessoas, PessoaAdapter.KEY_CLIENTE);

                }
            } catch (Exception e) {
                // Armazena as informacoes para para serem exibidas e enviadas
                ContentValues contentValues = new ContentValues();
                contentValues.put("comando", 0);
                contentValues.put("tela", "ProdutoListaMDFragment");
                contentValues.put("mensagem", getResources().getString(R.string.nao_consegimos_carregar_imagem_produtos) + " \n" + e.getMessage());
                contentValues.put("dados", e.toString());
                // Pega os dados do usuario
                FuncoesPersonalizadas funcoes = new FuncoesPersonalizadas(context);
                contentValues.put("usuario", funcoes.getValorXml("Usuario"));
                contentValues.put("empresa", funcoes.getValorXml("ChaveEmpresa"));
                contentValues.put("email", funcoes.getValorXml("Email"));
                // Exibe a mensagem
                funcoes.menssagem(contentValues);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressBarStatus.setVisibility(View.GONE);
            pesquisando = false;

            if ( (adapterPessoa != null) && (adapterPessoa.getCount() > 0) ){
                // Seta o listView com o novo adapter que ja esta com a nova lista
                listViewPessoa.setAdapter(adapterPessoa);
            }
            listViewPessoa.setSelection(positionScrollListViewPessoa);

        }
    }

}
