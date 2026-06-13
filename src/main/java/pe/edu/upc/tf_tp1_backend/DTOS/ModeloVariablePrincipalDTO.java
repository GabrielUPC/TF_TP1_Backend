package pe.edu.upc.tf_tp1_backend.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModeloVariablePrincipalDTO {

    @JsonProperty("variable")
    private String variable;

    @JsonProperty("valor")
    private Object valor;

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public Object getValor() {
        return valor;
    }

    public void setValor(Object valor) {
        this.valor = valor;
    }
}
