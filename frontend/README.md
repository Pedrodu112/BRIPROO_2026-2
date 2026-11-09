# NutriFollow — Frontend

Interface web do NutriFollow, desenvolvida com Next.js, React e TypeScript.

## Requisitos

- Node.js 20.9 ou superior.
- npm.
- Backend Spring Boot acessível em http://localhost:8080 para testar os fluxos integrados.

## Configuração local

Dentro desta pasta (frontend), copie o arquivo de ambiente de exemplo:

PowerShell:
```powershell
Copy-Item .env.example .env.local
```

Instale exatamente as dependências registradas no lockfile:

```powershell
npm ci
```

Confira os arquivos antes de executar:

```powershell
npm run lint
npx tsc --noEmit
npm run build
```

Para abrir a interface em desenvolvimento:

```powershell
npm run dev
```

Acesse http://localhost:3000.

A variável BACKEND_URL aponta para a API Spring Boot. O Next.js encaminha as chamadas de /api/backend/... para esse endereço; assim, o navegador não precisa realizar chamadas CORS diretamente ao servidor.

## Estado atual da integração

A tela de cadastro usa POST /usuarios. A API atual ainda não oferece autenticação, sessão, recuperação de senha ou edição de perfil. Por isso, o acesso por ID disponível na interface é explicitamente um **ambiente de demonstração**, não um login seguro. Não use esse modo como controle de acesso em produção.

Os fluxos integrados usam os endpoints existentes para registros, planos, acompanhamentos, publicações e interações. Algumas operações de perfil, busca/verificação profissional, favoritos e notificações não possuem endpoints no backend atual e não são simuladas.

A interface não consulta planos de pacientes usando apenas o ID do usuário: o endpoint de listagem atual não valida, na própria operação de leitura, o acompanhamento ativo e as permissões concedidas. A consulta profissional deve permanecer indisponível até essa regra ser aplicada no backend.

## Diretrizes de interface

- Formulários exibem somente os campos necessários para o tipo de dado.
- Ações destrutivas e decisões de acompanhamento usam confirmação contextual.
- Estados de carregamento, sucesso, erro e lista vazia recebem mensagens próprias.
- A interface mantém navegação por teclado, foco visível, link para pular a navegação, alvos de toque e layout responsivo.
- O movimento visual respeita prefers-reduced-motion.
