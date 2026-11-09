export type Registro = {
  id: number; tipo: string; registradoEm: string; descricao: string;
  tipoRefeicao: string | null; series: number | null; repeticoes: number | null;
  cargaKg: number | null; quantidadeMl: number | null; pesoKg: number | null;
  observacoes: string | null; ativo: boolean;
};
export type ItemPlano = {
  id: number; ordem: number; titulo: string; descricao: string | null;
  grupoMuscular: string | null; seriesPrevistas: number | null;
  repeticoesPrevistas: number | null; cargaPrevistaKg: number | null;
  descansoSegundos: number | null; horarioSugerido: string | null; ativo: boolean;
};
export type Plano = {
  id: number; usuarioId: number; tipo: string; titulo: string; objetivo: string | null;
  itens: ItemPlano[]; criadoPorId: number | null; criadoEm: string;
  versao: number; resumoUltimaAlteracao: string | null; ativo: boolean;
  atualizadoPorId: number | null; atualizadoEm: string | null;
};
export type Acompanhamento = {
  id: number; usuarioId: number; profissionalId: number; finalidade: string;
  status: string; permissoesConcedidas: string[]; solicitadoEm: string;
  respondidoEm: string | null; motivoRecusa: string | null;
  autorizadoEm: string | null; iniciadoEm: string | null;
  encerradoEm: string | null; encerradoPorId: number | null;
  motivoEncerramento: string | null;
};
export type Publicacao = {
  id: number; autorId: number; titulo: string; conteudo: string; ativa: boolean;
  publicadaEm: string; atualizadaEm: string | null; inativadaEm: string | null;
  motivoInativacao: string | null;
};
export type Interacao = {
  id: number; acompanhamentoId: number; tipo: string; autorId: number;
  destinatarioId: number | null; conteudo: string; prazo: string | null;
  situacaoMeta: string | null; criadaEm: string; atualizadaEm: string | null;
  lidaEm: string | null;
};
export type UsuarioResposta = { id: number; nome: string; email: string; ativo: boolean };

type ApiErrorPayload = { message?: unknown; detail?: unknown; error?: unknown; errors?: unknown };

function extractErrorMessage(payload: unknown, fallback: string): string {
  if (typeof payload === "string" && payload.trim()) return payload.trim();
  if (payload && typeof payload === "object") {
    const data = payload as ApiErrorPayload;
    for (const candidate of [data.message, data.detail, data.error]) {
      if (typeof candidate === "string" && candidate.trim()) return candidate.trim();
    }
    if (Array.isArray(data.errors)) {
      const messages = data.errors.map((entry) => {
        if (typeof entry === "string") return entry;
        if (entry && typeof entry === "object" && "defaultMessage" in entry) {
          const value = (entry as { defaultMessage?: unknown }).defaultMessage;
          return typeof value === "string" ? value : "";
        }
        return "";
      }).filter(Boolean);
      if (messages.length) return messages.join(" ");
    }
  }
  return fallback;
}

export class ApiError extends Error {
  readonly status: number;
  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const normalizedPath = path.startsWith("/") ? path : "/" + path;
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");
  if (init.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");

  let response: Response;
  try {
    response = await fetch("/api/backend" + normalizedPath, {
      ...init, headers, cache: "no-store",
    });
  } catch {
    throw new ApiError(
      "Não foi possível conectar ao servidor. Verifique se o backend está disponível e tente novamente.",
      0,
    );
  }

  const responseText = await response.text();
  let payload: unknown = null;
  if (responseText) {
    try { payload = JSON.parse(responseText) as unknown; }
    catch { payload = responseText; }
  }

  if (!response.ok) {
    const fallback = response.status >= 500
      ? "O servidor não conseguiu concluir a operação."
      : "Não foi possível concluir a solicitação.";
    throw new ApiError(extractErrorMessage(payload, fallback), response.status);
  }
  if (response.status === 204 || !responseText) return undefined as T;
  return payload as T;
}

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) return error.message;
  if (error instanceof Error && error.message) return error.message;
  return "Ocorreu um erro inesperado. Tente novamente.";
}

export function queryString(
  values: Record<string, string | number | boolean | null | undefined>,
): string {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(values)) {
    if (value === undefined || value === null || value === "") continue;
    params.set(key, String(value));
  }
  return params.toString();
}
