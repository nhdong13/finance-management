/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.baonq.enumeration;

/**
 * @author ADMIN
 */
public enum TransactionGroupType {
    INCOME(1), EXPENSE(2);
    private int type;

    TransactionGroupType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
