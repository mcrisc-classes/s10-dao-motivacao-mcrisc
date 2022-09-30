# IFBank
Transações Financeiras Imaginárias


## 1. Preparação do ambiente

Você precisará configurar o banco de dados `financas` em seu servidor MySQL.
Há duas alternativas:

1. Você pode restaurar o *dump* do banco de dados a partir do arquivo `resources/financas-dump.sql`; ou
1. Você pode criar apenas a estrutura (*schema*) das tabelas as partir do arquivo `resources/schema.sql`.

Execute o aplicativo `ifbank.gui.TransferApp`.
Realize algumas transferências e verifique o resultado por meio de `SELECT`'s diretamente no banco de dados. Verifique as alterações nas tabelas `conta` e `movimentacao`.


## 2. Tarefa

Finalize a implementação da versão _console_ do aplicativo TransferApp.
Edite o código da classe `ifbank.console.TransferApp` e realize as tarefas indicadas nos comentários com a palavra TODO.

Você deve,  _necessariamente_ , **reutilizar partes do código** presente na versão GUI (`ifbank.gui.TransferApp`).

Ao terminar, faça o push da sua implementação para o GitHub.

