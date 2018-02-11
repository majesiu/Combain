CREATE KEYSPACE IF NOT EXISTS Combine_Farm
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE Combine_Farm;

CREATE TABLE Zbiory (
  maszyna varchar,
  pojemnosc int,
  ilosc int,
  PRIMARY KEY (maszyna)
);

CREATE TABLE Pomiary (
  maszyna varchar,
  czujnik varchar,
  wartosc  double,
  wartosc_limit double,
  PRIMARY KEY (maszyna, czujnik)
);


CREATE TABLE Interwencje (
  stacja int,
  maszyna varchar,
  typ varchar,
  iteracja int,
  PRIMARY KEY (maszyna, typ, iteracja, stacja)
);

//drop table zbiory;
//drop tabke pomiary;
//drop table interwencje;