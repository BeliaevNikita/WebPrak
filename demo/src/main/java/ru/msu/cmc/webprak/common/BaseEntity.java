package ru.msu.cmc.webprak.common;

public interface BaseEntity<ID> {
    ID getId();
    void setId(ID id);
}
