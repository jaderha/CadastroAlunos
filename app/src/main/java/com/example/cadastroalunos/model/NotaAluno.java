package com.example.cadastroalunos.model;

import com.orm.SugarRecord;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class NotaAluno extends SugarRecord implements Serializable {
    Aluno aluno;
    Disciplina disciplina;
    Turma turma;
    Integer bimestre;
    Float nota;

    public static class NotaAlunoBuilder {

        public NotaAlunoBuilder with(AlunoTurma alunoTurma) {
            return this.aluno(alunoTurma.getAluno()).turma(alunoTurma.getTurma());
        }
    }
}
