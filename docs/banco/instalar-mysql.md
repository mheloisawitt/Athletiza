# Instalar o MySQL no Windows

Passo a passo para cada integrante deixar o banco do Athletiza funcionando no próprio computador.

## 1. MySQL Community Server 8.4 LTS

1. Acesse <https://dev.mysql.com/downloads/mysql/>, escolha a versão **8.4 LTS** e o sistema **Microsoft Windows**.
2. Baixe o **Windows (x86, 64-bit), MSI Installer**. Na página seguinte, clique em
   **"No thanks, just start my download"** (não precisa criar conta).
3. Instale com a opção **Typical** e deixe marcado **"Run MySQL Configurator"** no final.
4. No **MySQL Configurator**:

| Tela | O que escolher |
|---|---|
| Type and Networking | Config Type **Development Computer** (usa menos memória), porta **3306** |
| Accounts and Roles | Crie a **senha do root** e anote (ela vai no `db.properties`) |
| Windows Service | Marque *Configure MySQL Server as a Windows Service*. Para o computador não ficar lento, **desmarque** *Start the MySQL Server at System Startup* |
| Apply Configuration | **Execute** e depois **Finish** |

## 2. Ligar e desligar o MySQL

O serviço se chama **MySQL84**. Ligue antes de usar o sistema e desligue quando terminar:

- **Serviços:** Iniciar > digite **Serviços** (ou Win + R e `services.msc`) > botão direito em **MySQL84** > *Iniciar* / *Parar*;
- **Gerenciador de Tarefas:** Ctrl + Shift + Esc > aba **Serviços** > botão direito em **MySQL84**;
- **Terminal (como administrador):** `net start MySQL84` e `net stop MySQL84`.

Para ligar sempre com o Windows: clique duplo em **MySQL84** e mude *Tipo de inicialização* para **Automático**.

## 3. MySQL Workbench

1. Baixe em <https://dev.mysql.com/downloads/workbench/> e instale (se pedir o *Visual C++ Redistributable*, instale).
2. Na tela inicial, clique no **+** ao lado de **Database Connections** e preencha:
   - **Caption:** `Athletiza`
   - **Database Type:** MySQL
   - **Host Name:** `127.0.0.1`, **Port:** `3306`
   - **User Name:** `root`
   - **Store Password:** a senha do root
   - **Default Schema:** em branco na primeira vez (depois de criar o banco, use `athletiza`)
3. **Test Connection** e **OK**.
4. Abra a conexão e confira com `SELECT VERSION();` (Ctrl + Enter). Deve aparecer 8.4 ou superior.

## 4. Criar o banco do Athletiza

No Workbench, abra e execute, nesta ordem, os scripts de `src/main/resources/sql`:

1. `00_criar_banco.sql`: cria o banco `athletiza`;
2. `01_estrutura.sql`: cria as tabelas (rodar de novo apaga tudo e recria);
3. `02_dados_exemplo.sql`: dados de exemplo (login `admin`, senha `admin123`, com troca obrigatória no primeiro acesso).

Depois copie `db.properties.example` para `db.properties`, na raiz do projeto, e coloque a senha do root em `db.senha`.

## Problemas comuns

| Mensagem | Causa e solução |
|---|---|
| *Can't connect to MySQL server* / *Communications link failure* | O serviço **MySQL84** está parado: ligue-o |
| *Access denied for user 'root'* | Senha errada no Workbench ou no `db.properties` |
| *Unknown database 'athletiza'* | Falta executar o `00_criar_banco.sql` |
| *Table 'athletiza.usuario' doesn't exist* | Falta executar o `01_estrutura.sql` (e o `02_dados_exemplo.sql`) |

Não use XAMPP/WAMP: eles trazem o MariaDB, que tem diferenças em relação ao MySQL.
