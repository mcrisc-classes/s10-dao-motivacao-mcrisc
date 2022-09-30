use financas;

drop table if exists movimentacao;
drop table if exists conta;
drop table if exists pessoa;

create table pessoa (
	id int auto_increment not null primary key,
    nome varchar(40) not null
);
;
create table conta (
	numero int not null primary key,
    saldo decimal(11,2) default 0,
    titular int not null
);

alter table conta add constraint
foreign key titular_fk (titular) references pessoa (id);


create table movimentacao (
	id int auto_increment not null primary key,
    origem int not null,
    destino int not null,
    valor decimal(11, 2) not null,
	instante datetime not null default current_timestamp
);

alter table movimentacao add constraint
foreign key origem_fk (origem) references conta (numero);

alter table movimentacao add constraint
foreign key destino_fk (destino) references conta (numero);
