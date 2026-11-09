# NutriFollow — Backend

API REST do NutriFollow, desenvolvida em Java 25 e Spring Boot.

## Banco de dados SQL Server

A configuração versionada em `src/main/resources/application.properties` não contém senha. A aplicação obtém a credencial pelas variáveis de ambiente:

| Variável | Padrão / finalidade |
| --- | --- |
| `DB_URL` | Por padrão, SQL Server local na porta 1433, banco `NutriFollow` |
| `DB_USERNAME` | Por padrão, `sa` |
| `DB_PASSWORD` | Obrigatória; senha do usuário configurado no SQL Server |
| `DB_DDL_AUTO` | Por padrão, `update` |
| `DB_SHOW_SQL` | Por padrão, `false` |

Não coloque senhas em arquivos rastreados pelo Git nem as envie em commits, prints ou mensagens.

### Configuração temporária no PowerShell

Execute dentro da pasta `backend`. A senha é lida como entrada protegida e fica somente no ambiente do terminal atual:

```powershell
$env:DB_USERNAME = "sa"
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=NutriFollow;encrypt=true;trustServerCertificate=true"

$secure = Read-Host "Senha do SQL Server" -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try {
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
}
```

Para verificar apenas a compilação, use:

```powershell
.\mvnw.cmd clean compile
```

Para executar a API e conectar ao banco, use somente quando desejar iniciar o servidor:

```powershell
.\mvnw.cmd spring-boot:run
```

As variáveis definidas no PowerShell permanecem disponíveis enquanto essa janela estiver aberta. Ao fechar o terminal, defina-as novamente.

## Segurança

A API ainda precisa de autenticação e autorização baseadas em identidade comprovada antes de ser utilizada em produção. Os parâmetros de identificação enviados nas requisições não substituem um mecanismo de autenticação.
