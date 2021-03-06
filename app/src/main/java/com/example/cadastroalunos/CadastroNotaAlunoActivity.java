package com.example.cadastroalunos;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cadastroalunos.adapters.NotaAlunoAdapter;
import com.example.cadastroalunos.dao.SugarDAO;
import com.example.cadastroalunos.listeners.RecyclerItemClickListener;
import com.example.cadastroalunos.model.AlunoTurma;
import com.example.cadastroalunos.model.Disciplina;
import com.example.cadastroalunos.model.DisciplinaTurma;
import com.example.cadastroalunos.model.NotaAluno;
import com.example.cadastroalunos.model.Turma;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fr.ganfra.materialspinner.MaterialSpinner;

public class CadastroNotaAlunoActivity extends BaseActivity implements View.OnClickListener {
    private RecyclerView rvListaAlunos;
    private LinearLayout lnNotaAluno;
    private RadioGroup rgRegimeTurma;
    MaterialSpinner spTurma;
    MaterialSpinner spDisciplina;
    Turma turmaSelecionada;
    Disciplina disciplinaSelecionada;
    int regimeSelecionado;
    List<Disciplina> disciplinaTurmas = new ArrayList<>();
    private List<NotaAluno> listaAlunos;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.NotaAlunoTitle);
        setContentView(R.layout.activity_cadastro_nota_aluno);
        loadComponents();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    void loadComponents() {
        lnNotaAluno = findViewById(R.id.lnNotaAluno);
        rvListaAlunos = findViewById(R.id.rvListaAlunos);
        spTurma = findViewById(R.id.spTurma);
        rgRegimeTurma = findViewById(R.id.rgRegimeTurma);
        spDisciplina = findViewById(R.id.spDisciplina);
        List<Turma> turmas = SugarDAO.retornaObjetos(Turma.class, "nome asc");
        spTurma.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, turmas));
        spDisciplina.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, disciplinaTurmas));

        spTurma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spTurma.getSelectedItemPosition() != 0) {
                    turmaSelecionada = turmas.get(spTurma.getSelectedItemPosition() - 1);
                    disciplinaTurmas.clear();
                    List<DisciplinaTurma> test = com.example.cadastroalunos.model.DisciplinaTurma.find(DisciplinaTurma.class, " turma = ? ", turmaSelecionada.getId().toString());
                    disciplinaTurmas.addAll(test.stream().map(DisciplinaTurma::getDisciplina).collect(Collectors.toList()));
                    spDisciplina.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (spDisciplina.getSelectedItemPosition() != 0) {
                                disciplinaSelecionada = disciplinaTurmas.get(spDisciplina.getSelectedItemPosition() - 1);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            disciplinaSelecionada = null;
                        }
                    });
                    criarRadioButtons(turmaSelecionada);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                turmaSelecionada = null;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void criarRadioButtons(Turma turmaSelecionada) {
        rgRegimeTurma.removeAllViews();
        IntStream.range(1, turmaSelecionada.getRegimeTurma().getQtdBimestres() + 1).forEach(i -> {
            RadioButton rdbtn = new RadioButton(this);
            rdbtn.setId(View.generateViewId());
            rdbtn.setText(String.valueOf(i));
            rdbtn.setOnClickListener(view -> {
                regimeSelecionado = i;
                criaListaAlunos();
            });
            rgRegimeTurma.addView(rdbtn);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void criaListaAlunos() {
        listaAlunos = criaNotaAluno();
        NotaAlunoAdapter adapter = new NotaAlunoAdapter(listaAlunos, this);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        Intent intentLancaNota = new Intent(this, LancaNotaAlunoActivity.class);
        rvListaAlunos.setLayoutManager(llm);

        rvListaAlunos.setAdapter(adapter);
        rvListaAlunos.addOnItemTouchListener(new RecyclerItemClickListener(this, rvListaAlunos, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                NotaAluno notaAlunoSelecionado = listaAlunos.get(position);
                intentLancaNota.putExtra("notaAluno", notaAlunoSelecionado);
                intentLancaNota.putExtra("idAluno", notaAlunoSelecionado.getAluno().getId());
                intentLancaNota.putExtra("idTurma", notaAlunoSelecionado.getTurma().getId());
                intentLancaNota.putExtra("idDisciplina", disciplinaSelecionada.getId());
                intentLancaNota.putExtra("idNotaAluno", notaAlunoSelecionado.getId());
                startActivity(intentLancaNota);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        criaListaAlunos();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<NotaAluno> criaNotaAluno() {
        if (nonNull(turmaSelecionada))
            return AlunoTurma.find(AlunoTurma.class, " turma = ?", turmaSelecionada.getId().toString()).stream()
                    .map(alunoTurma -> {
                        List<NotaAluno> alunos = NotaAluno.find(
                                NotaAluno.class, " aluno = ? and turma = ? and disciplina = ? and bimestre = ? ",
                                alunoTurma.getAluno().getId().toString(),
                                alunoTurma.getTurma().getId().toString(),
                                disciplinaSelecionada.getId().toString(),
                                String.valueOf(regimeSelecionado));
                        if (!alunos.isEmpty()) {
                            return alunos.get(0);
                        }
                        return NotaAluno.builder()
                                .with(alunoTurma)
                                .disciplina(disciplinaSelecionada)
                                .bimestre(regimeSelecionado)
                                .build();
                    }).collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Override
    void salvar() {

    }

    @Override
    public void onClick(View view) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    void limparCampos() {
        spTurma.setSelection(0);
        spDisciplina.setSelection(0);
        rgRegimeTurma.removeAllViews();
        turmaSelecionada = null;
        disciplinaSelecionada = null;
        regimeSelecionado = 0;
        listaAlunos = new ArrayList<>();
        criaListaAlunos();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    void validaCamposExtras(AtomicBoolean valido) {
        if (isNull(spDisciplina.getSelectedItem())) {
            spDisciplina.setError("Selecione uma Disciplina");
            spDisciplina.requestFocus();
            valido.set(false);
        }
        if (isNull(spTurma.getSelectedItem())) {
            spTurma.setError("Selecione uma Turma");
            spTurma.requestFocus();
            valido.set(false);
        }
        if (regimeSelecionado == 0) {
            rgRegimeTurma.requestFocus();
            valido.set(false);
        }
    }
}