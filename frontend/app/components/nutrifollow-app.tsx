"use client";

import { useEffect, useState, type FormEvent, type ReactNode } from "react";
import {
  apiRequest,
  errorMessage,
  queryString,
  type Acompanhamento,
  type Interacao,
  type Plano,
  type Publicacao,
  type Registro,
  type UsuarioResposta,
} from "../lib/api";

type Role = "COMUM" | "NUTRICIONISTA" | "EDUCACAO_FISICA" | "ADMINISTRADOR";
type View =
  | "inicio"
  | "registros"
  | "planos"
  | "acompanhamentos"
  | "comunidade"
  | "mensagens"
  | "administracao";

type DemoContext = { id: number; role: Role; name: string };
type Notice = { kind: "success" | "error" | "info"; text: string };
type ConfirmAction =
  | { kind: "recusar"; id: number }
  | { kind: "encerrar"; id: number }
  | { kind: "inativarPublicacao"; id: number }
  | { kind: "inativarRegistro"; id: number }
  | { kind: "inativarPlano"; id: number }
  | null;
type PlanItemDraft = {
  titulo: string;
  descricao: string;
  grupoMuscular: string;
  seriesPrevistas: string;
  repeticoesPrevistas: string;
  cargaPrevistaKg: string;
  descansoSegundos: string;
  horarioSugerido: string;
};

const emptyPlanItem: PlanItemDraft = {
  titulo: "",
  descricao: "",
  grupoMuscular: "",
  seriesPrevistas: "3",
  repeticoesPrevistas: "12",
  cargaPrevistaKg: "",
  descansoSegundos: "60",
  horarioSugerido: "",
};

function permissionsForFinalidade(finalidade: string) {
  const category = finalidade === "NUTRICAO" ? "ALIMENTAR" : "TREINO";
  return [
    category + ":CRIAR",
    category + ":ALTERAR",
    "INTERACAO:OBSERVACAO",
    "INTERACAO:ALTERAR_OBSERVACAO",
    "INTERACAO:META",
    "INTERACAO:ALTERAR_META",
  ];
}

function defaultPermissionsForFinalidade(finalidade: string) {
  const category = finalidade === "NUTRICAO" ? "ALIMENTAR" : "TREINO";
  return [category + ":CRIAR", category + ":ALTERAR", "INTERACAO:OBSERVACAO"];
}

function Glyph({ name, size = 18 }: { name: string; size?: number }) {
  const common = {
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.8,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
  };
  const shapes: Record<string, ReactNode> = {
    grid: <><rect x="3" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="3" width="7" height="7" rx="1.5" /><rect x="3" y="14" width="7" height="7" rx="1.5" /><rect x="14" y="14" width="7" height="7" rx="1.5" /></>,
    activity: <><path d="M3 12h4l3-8 4 16 3-8h4" /></>,
    book: <><path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v17H6.5A2.5 2.5 0 0 0 4 22z" /><path d="M4 5.5v14A2.5 2.5 0 0 1 6.5 17H20" /></>,
    users: <><path d="M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="10" cy="7" r="4" /><path d="M20 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" /></>,
    message: <><path d="M21 11.5a8.5 8.5 0 0 1-12.3 7.6L3 21l1.9-5.7A8.5 8.5 0 1 1 21 11.5Z" /></>,
    heart: <><path d="M20.8 8.8c0 5.4-8.8 11-8.8 11S3.2 14.2 3.2 8.8A4.7 4.7 0 0 1 12 6.1a4.7 4.7 0 0 1 8.8 2.7Z" /></>,
    plus: <><path d="M12 5v14M5 12h14" /></>,
    arrow: <><path d="M5 12h14M13 6l6 6-6 6" /></>,
    logout: <><path d="M10 17l5-5-5-5M15 12H3" /><path d="M12 3h6a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-6" /></>,
    shield: <><path d="M12 22s8-4 8-11V5l-8-3-8 3v6c0 7 8 11 8 11Z" /><path d="m9 12 2 2 4-4" /></>,
    refresh: <><path d="M20 7v5h-5M4 17v-5h5" /><path d="M5.6 9a7 7 0 0 1 11.6-2L20 12M4 12l2.8 5a7 7 0 0 0 11.6-2" /></>,
    clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
    check: <><path d="m5 12 4 4L19 6" /></>,
    warning: <><path d="M10.3 4.3 2.5 18a2 2 0 0 0 1.7 3h15.6a2 2 0 0 0 1.7-3L13.7 4.3a2 2 0 0 0-3.4 0Z" /><path d="M12 9v4M12 17h.01" /></>,
  };

  return (
    <svg width={size} height={size} viewBox="0 0 24 24" aria-hidden="true" {...common}>
      {shapes[name] ?? shapes.grid}
    </svg>
  );
}

function formatDate(value?: string | null) {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(date);
}

function roleLabel(role: Role) {
  const labels: Record<Role, string> = {
    COMUM: "Usuário",
    NUTRICIONISTA: "Nutricionista",
    EDUCACAO_FISICA: "Educação física",
    ADMINISTRADOR: "Administrador",
  };
  return labels[role];
}

function statusLabel(value: string) {
  const labels: Record<string, string> = {
    PENDENTE: "Pendente",
    ACEITO: "Aguardando autorização",
    ATIVO: "Ativo",
    RECUSADO: "Recusado",
    ENCERRADO: "Encerrado",
    TREINO: "Treino",
    REFEICAO: "Refeição",
    AGUA: "Água",
    PESO: "Peso",
    ALIMENTAR: "Alimentar",
    MENSAGEM: "Mensagem",
    OBSERVACAO: "Observação",
    META: "Meta",
    NUTRICAO: "Nutrição",
    EDUCACAO_FISICA: "Educação física",
    EM_ANDAMENTO: "Em andamento",
    CONCLUIDA: "Concluída",
    CANCELADA: "Cancelada",
  };
  return labels[value] ?? value;
}

function Field({
  label,
  children,
  hint,
}: {
  label: string;
  children: ReactNode;
  hint?: string;
}) {
  return (
    <label className="field">
      <span className="field-label">{label}</span>
      {children}
      {hint ? <span className="field-hint">{hint}</span> : null}
    </label>
  );
}

export default function NutriFollowApp() {
  const [entryMode, setEntryMode] = useState<"register" | "demo">("register");
  const [registerName, setRegisterName] = useState("");
  const [registerEmail, setRegisterEmail] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const [demoId, setDemoId] = useState("");
  const [demoRole, setDemoRole] = useState<Role>("COMUM");
  const [workspace, setWorkspace] = useState<DemoContext | null>(null);
  const [view, setView] = useState<View>("inicio");
  const [notice, setNotice] = useState<Notice | null>(null);
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  const [records, setRecords] = useState<Registro[]>([]);
  const [editingRecordId, setEditingRecordId] = useState<number | null>(null);
  const [plans, setPlans] = useState<Plano[]>([]);
  const [editingPlanId, setEditingPlanId] = useState<number | null>(null);
  const [editingPlanItemIndex, setEditingPlanItemIndex] = useState<number | null>(null);
  const [followups, setFollowups] = useState<Acompanhamento[]>([]);
  const [publications, setPublications] = useState<Publicacao[]>([]);
  const [interactions, setInteractions] = useState<Interacao[]>([]);
  const [interactionToEdit, setInteractionToEdit] = useState<Interacao | null>(null);
  const [interactionEditContent, setInteractionEditContent] = useState("");
  const [interactionEditDeadline, setInteractionEditDeadline] = useState("");
  const [selectedFollowId, setSelectedFollowId] = useState("");

  const [recordDraft, setRecordDraft] = useState({
    tipo: "TREINO",
    descricao: "",
    tipoRefeicao: "ALMOCO",
    series: "3",
    repeticoes: "12",
    cargaKg: "0",
    quantidadeMl: "500",
    pesoKg: "",
    observacoes: "",
  });

  const [planKind, setPlanKind] = useState<"TREINO" | "ALIMENTAR">("TREINO");
  const [planTitle, setPlanTitle] = useState("");
  const [planFollowId, setPlanFollowId] = useState("");
  const [itemDraft, setItemDraft] = useState<PlanItemDraft>(emptyPlanItem);
  const [planItems, setPlanItems] = useState<Record<string, unknown>[]>([]);

  const [professionalId, setProfessionalId] = useState("");
  const [finalidade, setFinalidade] = useState("NUTRICAO");
  const [permissionDrafts, setPermissionDrafts] = useState<Record<string, string[]>>({});

  const [publicationTitle, setPublicationTitle] = useState("");
  const [editingPublicationId, setEditingPublicationId] = useState<number | null>(null);
  const [publicationContent, setPublicationContent] = useState("");
  const [messageDraft, setMessageDraft] = useState("");
  const [interactionKind, setInteractionKind] = useState<"MENSAGEM" | "OBSERVACAO" | "META">("MENSAGEM");
  const [interactionDeadline, setInteractionDeadline] = useState("");
  const [interactionSituation, setInteractionSituation] = useState("PENDENTE");
  const [confirmation, setConfirmation] = useState<ConfirmAction>(null);
  const [confirmationReason, setConfirmationReason] = useState("");

  const isCommon = workspace?.role === "COMUM";
  const isProfessional =
    workspace?.role === "NUTRICIONISTA" ||
    workspace?.role === "EDUCACAO_FISICA";

  function selectedPermissionsFor(item: Acompanhamento) {
    return permissionDrafts[String(item.id)] ?? defaultPermissionsForFinalidade(item.finalidade);
  }

  function toggleFollowupPermission(item: Acompanhamento, permission: string, checked: boolean) {
    setPermissionDrafts((current) => {
      const selected = current[String(item.id)] ?? defaultPermissionsForFinalidade(item.finalidade);
      const next = checked
        ? [...selected, permission]
        : selected.filter((value) => value !== permission);
      return { ...current, [String(item.id)]: next };
    });
  }

  const eligiblePlanFollowups = isProfessional
    ? followups.filter((item) =>
        item.status === "ATIVO" &&
        item.profissionalId === workspace?.id &&
        item.finalidade === (planKind === "ALIMENTAR" ? "NUTRICAO" : "EDUCACAO_FISICA") &&
        (item.permissoesConcedidas ?? []).includes(planKind + (editingPlanId === null ? ":CRIAR" : ":ALTERAR"))
      )
    : [];

  const todayKey = new Date().toDateString();
  const todayRecords = records.filter((item) => new Date(item.registradoEm).toDateString() === todayKey);
  const latestWeightRecord = [...records]
    .filter((item) => item.tipo === "PESO" && item.pesoKg != null)
    .sort((a, b) => new Date(b.registradoEm).getTime() - new Date(a.registradoEm).getTime())[0];

  const navItems: { id: View; label: string; icon: string }[] =
    workspace?.role === "COMUM"
      ? [
          { id: "inicio", label: "Visão geral", icon: "grid" },
          { id: "registros", label: "Meus registros", icon: "activity" },
          { id: "planos", label: "Meus planos", icon: "book" },
          { id: "acompanhamentos", label: "Acompanhamentos", icon: "users" },
          { id: "comunidade", label: "Comunidade", icon: "heart" },
          { id: "mensagens", label: "Conversas", icon: "message" },
        ]
      : workspace?.role === "ADMINISTRADOR"
        ? [
            { id: "inicio", label: "Visão geral", icon: "grid" },
            { id: "administracao", label: "Administração", icon: "shield" },
          ]
        : [
            { id: "inicio", label: "Visão geral", icon: "grid" },
            { id: "acompanhamentos", label: "Pacientes", icon: "users" },
            { id: "planos", label: "Planos", icon: "book" },
            { id: "comunidade", label: "Publicações", icon: "heart" },
            { id: "mensagens", label: "Conversas", icon: "message" },
          ];

  useEffect(() => {
    if (!confirmation && !interactionToEdit) return;
    function handleDialogKey(event: KeyboardEvent) {
      if (event.key === "Escape" && !busy) {
        setConfirmation(null);
        setConfirmationReason("");
        setInteractionToEdit(null);
      }
    }
    window.addEventListener("keydown", handleDialogKey);
    return () => window.removeEventListener("keydown", handleDialogKey);
  }, [confirmation, interactionToEdit, busy]);

  useEffect(() => {
    if (!workspace) return;
    let active = true;

    async function load() {
      setLoading(true);
      try {
        if (view === "administracao") return;

        if (view === "inicio") {
          if (workspace.role === "COMUM") {
            const results = await Promise.all([
              apiRequest<Registro[]>(
                "/usuarios/" + workspace.id + "/registros",
              ),
              apiRequest<Plano[]>("/planos/usuario/" + workspace.id),
              apiRequest<Acompanhamento[]>(
                "/acompanhamentos/usuario/" + workspace.id,
              ),
              apiRequest<Publicacao[]>("/publicacoes"),
            ]);
            if (!active) return;
            setRecords(results[0]);
            setPlans(results[1]);
            setFollowups(results[2]);
            setPublications(results[3]);
          } else if (
            workspace.role === "NUTRICIONISTA" ||
            workspace.role === "EDUCACAO_FISICA"
          ) {
            const results = await Promise.all([
              apiRequest<Acompanhamento[]>(
                "/acompanhamentos/profissional/" + workspace.id,
              ),
              apiRequest<Publicacao[]>("/publicacoes"),
            ]);
            if (!active) return;
            setFollowups(results[0]);
            setPublications(results[1]);
          }
        } else if (view === "registros" && workspace.role === "COMUM") {
          const data = await apiRequest<Registro[]>(
            "/usuarios/" + workspace.id + "/registros",
          );
          if (active) setRecords(data);
        } else if (view === "planos") {
          if (workspace.role === "COMUM") {
            const data = await apiRequest<Plano[]>(
              "/planos/usuario/" + workspace.id,
            );
            if (active) setPlans(data);
          } else if (workspace.role === "ADMINISTRADOR") {
            return;
          }
        } else if (view === "acompanhamentos") {
          const path =
            workspace.role === "COMUM"
              ? "/acompanhamentos/usuario/" + workspace.id
              : "/acompanhamentos/profissional/" + workspace.id;
          const data = await apiRequest<Acompanhamento[]>(path);
          if (!active) return;
          setFollowups(data);
          if (data.length > 0) {
            setSelectedFollowId((current) => current || String(data[0].id));
          }
        } else if (view === "comunidade") {
          const data = await apiRequest<Publicacao[]>("/publicacoes");
          if (active) setPublications(data);
        } else if (view === "mensagens") {
          const data =
            workspace.role === "COMUM"
              ? await apiRequest<Acompanhamento[]>(
                  "/acompanhamentos/usuario/" + workspace.id,
                )
              : await apiRequest<Acompanhamento[]>(
                  "/acompanhamentos/profissional/" + workspace.id,
                );
          if (!active) return;
          setFollowups(data);
          if (data.length > 0) {
            setSelectedFollowId((current) => current || String(data[0].id));
          }
        }
      } catch (error) {
        if (active) {
          setNotice({ kind: "error", text: errorMessage(error) });
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    void load();
    return () => {
      active = false;
    };
  }, [workspace, view, reloadKey]);

  useEffect(() => {
    if (!workspace || view !== "mensagens" || !selectedFollowId) return;
    let active = true;
    apiRequest<Interacao[]>(
      "/interacoes/acompanhamento/" +
        selectedFollowId +
        "?usuarioId=" +
        workspace.id,
    )
      .then((data) => {
        if (active) setInteractions(data);
      })
      .catch((error: unknown) => {
        if (active) setNotice({ kind: "error", text: errorMessage(error) });
      });
    return () => {
      active = false;
    };
  }, [workspace, view, selectedFollowId, reloadKey]);

  useEffect(() => {
    if (workspace?.role === "NUTRICIONISTA") setPlanKind("ALIMENTAR");
    else setPlanKind("TREINO");
  }, [workspace?.role]);

  async function runAction(action: () => Promise<unknown>, success: string): Promise<boolean> {
    setBusy(true);
    setNotice(null);
    try {
      await action();
      setNotice({ kind: "success", text: success });
      setReloadKey((value) => value + 1);
      return true;
    } catch (error) {
      setNotice({ kind: "error", text: errorMessage(error) });
      return false;
    } finally {
      setBusy(false);
    }
  }

  async function register(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setNotice(null);
    try {
      const created = await apiRequest<UsuarioResposta>("/usuarios", {
        method: "POST",
        body: JSON.stringify({
          nome: registerName,
          email: registerEmail,
          senha: registerPassword,
        }),
      });
      setWorkspace({
        id: created.id,
        role: "COMUM",
        name: created.nome,
      });
      setView("inicio");
      setRegisterPassword("");
      setNotice({
        kind: "success",
        text:
          "Conta criada. Seu identificador é " +
          created.id +
          ". O acesso abaixo é um contexto de demonstração, não uma sessão autenticada.",
      });
    } catch (error) {
      setNotice({ kind: "error", text: errorMessage(error) });
    } finally {
      setBusy(false);
    }
  }

  function enterDemo(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const id = Number(demoId);
    if (!Number.isInteger(id) || id <= 0) {
      setNotice({ kind: "error", text: "Informe um ID numérico válido." });
      return;
    }
    setWorkspace({
      id,
      role: demoRole,
      name: "Conta de demonstração",
    });
    setView("inicio");
    setNotice({
      kind: "info",
      text:
        "Modo de demonstração: o ID e o perfil são informados manualmente. O backend atual ainda não fornece login ou sessão autenticada.",
    });
  }

  function editRecord(item: Registro) {
    setEditingRecordId(item.id);
    setRecordDraft({
      tipo: item.tipo,
      descricao: item.descricao,
      tipoRefeicao: item.tipoRefeicao ?? "ALMOCO",
      series: item.series == null ? "3" : String(item.series),
      repeticoes: item.repeticoes == null ? "12" : String(item.repeticoes),
      cargaKg: item.cargaKg == null ? "0" : String(item.cargaKg),
      quantidadeMl: item.quantidadeMl == null ? "500" : String(item.quantidadeMl),
      pesoKg: item.pesoKg == null ? "" : String(item.pesoKg),
      observacoes: item.observacoes ?? "",
    });
    setNotice({ kind: "info", text: "Editando o registro #" + item.id + ". O tipo do registro não pode ser alterado." });
  }

  function cancelRecordEdit() {
    setEditingRecordId(null);
    setRecordDraft({
      tipo: "TREINO", descricao: "", tipoRefeicao: "ALMOCO",
      series: "3", repeticoes: "12", cargaKg: "0",
      quantidadeMl: "500", pesoKg: "", observacoes: "",
    });
    setNotice(null);
  }

  async function createRecord(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!workspace) return;
    const body = {
      tipo: recordDraft.tipo,
      descricao: recordDraft.descricao.trim(),
      tipoRefeicao: recordDraft.tipo === "REFEICAO" ? recordDraft.tipoRefeicao : null,
      series: recordDraft.tipo === "TREINO" ? Number(recordDraft.series) : null,
      repeticoes: recordDraft.tipo === "TREINO" ? Number(recordDraft.repeticoes) : null,
      cargaKg: recordDraft.tipo === "TREINO" ? Number(recordDraft.cargaKg) : null,
      quantidadeMl: recordDraft.tipo === "AGUA" ? Number(recordDraft.quantidadeMl) : null,
      pesoKg: recordDraft.tipo === "PESO" ? Number(recordDraft.pesoKg) : null,
      observacoes: recordDraft.observacoes.trim() || null,
    };
    const endpoint = "/usuarios/" + workspace.id + "/registros";
    const editing = editingRecordId !== null;
    const saved = await runAction(
      () => apiRequest(
        editing ? endpoint + "/" + editingRecordId : endpoint,
        { method: editing ? "PUT" : "POST", body: JSON.stringify(body) },
      ),
      editing ? "Registro atualizado." : "Registro salvo.",
    );
    if (saved) {
      setEditingRecordId(null);
      setRecordDraft({
        tipo: "TREINO", descricao: "", tipoRefeicao: "ALMOCO",
        series: "3", repeticoes: "12", cargaKg: "0",
        quantidadeMl: "500", pesoKg: "", observacoes: "",
      });
    }
  }

  function editPlanItem(index: number) {
    const item = planItems[index];
    if (!item) return;
    const value = (key: string) => item[key] == null ? "" : String(item[key]);
    setEditingPlanItemIndex(index);
    setItemDraft({
      titulo: value("titulo"),
      descricao: value("descricao"),
      grupoMuscular: value("grupoMuscular"),
      seriesPrevistas: value("seriesPrevistas") || "3",
      repeticoesPrevistas: value("repeticoesPrevistas") || "12",
      cargaPrevistaKg: value("cargaPrevistaKg"),
      descansoSegundos: value("descansoSegundos") || "60",
      horarioSugerido: value("horarioSugerido"),
    });
    setNotice({ kind: "info", text: "Editando o item " + (index + 1) + " do plano." });
  }

  function addPlanItem() {
    if (!itemDraft.titulo.trim()) {
      setNotice({ kind: "error", text: "Informe o título do item do plano." });
      return;
    }

    let newItem: Record<string, unknown>;
    if (planKind === "TREINO") {
      if (Number(itemDraft.seriesPrevistas) <= 0 || Number(itemDraft.repeticoesPrevistas) <= 0) {
        setNotice({ kind: "error", text: "Séries e repetições precisam ser maiores que zero." });
        return;
      }
      if (itemDraft.cargaPrevistaKg !== "" && Number(itemDraft.cargaPrevistaKg) < 0) {
        setNotice({ kind: "error", text: "A carga não pode ser negativa." });
        return;
      }
      if (itemDraft.descansoSegundos !== "" && Number(itemDraft.descansoSegundos) < 0) {
        setNotice({ kind: "error", text: "O descanso não pode ser negativo." });
        return;
      }
      newItem = {
        ordem: editingPlanItemIndex === null ? planItems.length + 1 : Number(planItems[editingPlanItemIndex]?.ordem ?? editingPlanItemIndex + 1),
        titulo: itemDraft.titulo.trim(),
        descricao: itemDraft.descricao.trim() || null,
        grupoMuscular: itemDraft.grupoMuscular.trim() || null,
        seriesPrevistas: Number(itemDraft.seriesPrevistas),
        repeticoesPrevistas: Number(itemDraft.repeticoesPrevistas),
        cargaPrevistaKg: itemDraft.cargaPrevistaKg === "" ? null : Number(itemDraft.cargaPrevistaKg),
        descansoSegundos: itemDraft.descansoSegundos === "" ? null : Number(itemDraft.descansoSegundos),
        horarioSugerido: itemDraft.horarioSugerido.trim() || null,
      };
    } else {
      if (!itemDraft.descricao.trim()) {
        setNotice({ kind: "error", text: "A descrição da orientação alimentar é obrigatória." });
        return;
      }
      newItem = {
        ordem: editingPlanItemIndex === null ? planItems.length + 1 : Number(planItems[editingPlanItemIndex]?.ordem ?? editingPlanItemIndex + 1),
        titulo: itemDraft.titulo.trim(),
        descricao: itemDraft.descricao.trim(),
        grupoMuscular: null,
        seriesPrevistas: null,
        repeticoesPrevistas: null,
        cargaPrevistaKg: null,
        descansoSegundos: null,
        horarioSugerido: itemDraft.horarioSugerido.trim() || null,
      };
    }

    if (editingPlanItemIndex !== null) {
      setPlanItems((current) => current.map((item, index) => index === editingPlanItemIndex ? newItem : item));
      setNotice({ kind: "success", text: "Item atualizado no rascunho." });
    } else {
      setPlanItems((current) => [...current, newItem]);
      setNotice({ kind: "success", text: "Item adicionado ao rascunho do plano." });
    }
    setEditingPlanItemIndex(null);
    setItemDraft(emptyPlanItem);
  }

  function cancelPlanEdit() {
    setEditingPlanId(null);
    setEditingPlanItemIndex(null);
    setPlanItems([]);
    setPlanTitle("");
    setPlanFollowId("");
    setItemDraft(emptyPlanItem);
    setNotice(null);
  }

  function startEditingPlan(plan: Plano) {
    const activeItems = (plan.itens ?? []).filter((item) => item.ativo !== false);
    if (!activeItems.length) {
      setNotice({ kind: "error", text: "Este plano não possui itens ativos para alterar." });
      return;
    }

    if (isProfessional) {
      const follow = followups.find((item) =>
        item.status === "ATIVO" &&
        item.profissionalId === workspace?.id &&
        item.usuarioId === plan.usuarioId &&
        item.finalidade === (plan.tipo === "ALIMENTAR" ? "NUTRICAO" : "EDUCACAO_FISICA") &&
        (item.permissoesConcedidas ?? []).includes(plan.tipo + ":ALTERAR")
      );
      if (!follow) {
        setNotice({ kind: "error", text: "Não há acompanhamento ativo correspondente para autorizar esta alteração." });
        return;
      }
      setPlanFollowId(String(follow.id));
    }

    setEditingPlanId(plan.id);
    setPlanKind(plan.tipo === "ALIMENTAR" ? "ALIMENTAR" : "TREINO");
    setPlanTitle(plan.titulo);
    setPlanItems(activeItems.map((item) => ({
      ordem: item.ordem,
      titulo: item.titulo,
      descricao: item.descricao,
      grupoMuscular: item.grupoMuscular,
      seriesPrevistas: item.seriesPrevistas,
      repeticoesPrevistas: item.repeticoesPrevistas,
      cargaPrevistaKg: item.cargaPrevistaKg,
      descansoSegundos: item.descansoSegundos,
      horarioSugerido: item.horarioSugerido,
    })));
    setEditingPlanItemIndex(null);
    setItemDraft(emptyPlanItem);
    setView("planos");
    setNotice({ kind: "info", text: "A edição mantém a quantidade e a ordem dos itens ativos, conforme a regra atual da API." });
  }

  async function createPlan(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!workspace || planItems.length === 0) {
      setNotice({ kind: "error", text: "Adicione pelo menos um item antes de salvar o plano." });
      return;
    }

    const editing = editingPlanId !== null;
    let path = "";
    let method: "POST" | "PUT" = editing ? "PUT" : "POST";

    if (workspace.role === "COMUM") {
      if (editing) {
        path = "/planos/" + editingPlanId + "/usuario?" + queryString({ usuarioId: workspace.id });
      } else {
        if (!planTitle.trim()) {
          setNotice({ kind: "error", text: "Informe o título do plano." });
          return;
        }
        path = "/planos/treino/usuario?" + queryString({ usuarioId: workspace.id, titulo: planTitle.trim() });
      }
    } else if (isProfessional) {
      const selectedFollow = followups.find((item) =>
        String(item.id) === planFollowId &&
        item.profissionalId === workspace.id &&
        item.status === "ATIVO" &&
        item.finalidade === (planKind === "ALIMENTAR" ? "NUTRICAO" : "EDUCACAO_FISICA") &&
        (item.permissoesConcedidas ?? []).includes(planKind + (editing ? ":ALTERAR" : ":CRIAR"))
      );
      if (!selectedFollow) {
        setNotice({ kind: "error", text: "Selecione um acompanhamento ativo da sua especialidade." });
        return;
      }

      if (!editing && planKind === "TREINO" && !planTitle.trim()) {
        setNotice({ kind: "error", text: "Informe o título do plano de treino." });
        return;
      }

      if (editing) {
        path = "/planos/" + editingPlanId + "?" + queryString({
          profissionalId: workspace.id,
          acompanhamentoId: selectedFollow.id,
        });
      } else {
        const endpoint = planKind === "ALIMENTAR" ? "/planos/alimentar" : "/planos/treino";
        path = endpoint + "?" + queryString({
          profissionalId: workspace.id,
          acompanhamentoId: selectedFollow.id,
          titulo: planKind === "TREINO" ? planTitle.trim() : undefined,
        });
      }
    } else {
      setNotice({ kind: "error", text: "Este perfil não possui permissão para gerenciar planos." });
      return;
    }

    const succeeded = await runAction(
      async () => {
        const saved = await apiRequest<Plano>(path, {
          method,
          body: JSON.stringify({ itens: planItems }),
        });
        if (isProfessional) {
          setPlans((current) => editing
            ? current.map((item) => item.id === saved.id ? saved : item)
            : [saved, ...current.filter((item) => item.id !== saved.id)]);
        }
      },
      editing ? "Plano atualizado. A autoria e a versão foram registradas pelo backend." : "Plano criado e persistido.",
    );

    if (succeeded) {
      setEditingPlanId(null);
      setEditingPlanItemIndex(null);
      setPlanItems([]);
      setPlanTitle("");
      setPlanFollowId("");
      setItemDraft(emptyPlanItem);
    }
  }

  async function requestFollowup(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!workspace) return;
    const professional = Number(professionalId);
    if (!Number.isInteger(professional) || professional <= 0) {
      setNotice({
        kind: "error",
        text: "Informe o ID do profissional.",
      });
      return;
    }
    const succeeded = await runAction(
      () =>
        apiRequest(
          "/acompanhamentos?" +
            queryString({
              usuarioId: workspace.id,
              profissionalId: professional,
            }),
          {
            method: "POST",
            body: JSON.stringify({ finalidade }),
          },
        ),
      "Solicitação enviada ao profissional.",
    );
    if (succeeded) setProfessionalId("");
  }

  async function followupAction(
    id: number,
    action: "aceitar" | "recusar" | "autorizar" | "encerrar",
  ) {
    if (!workspace) return;
    let path = "";
    let body: string | undefined;
    if (action === "aceitar") {
      path =
        "/acompanhamentos/" +
        id +
        "/aceitar?" +
        queryString({ profissionalId: workspace.id });
    } else if (action === "recusar") {
      setConfirmation({ kind: "recusar", id });
      setConfirmationReason("");
      return;
    } else if (action === "autorizar") {
      const follow = followups.find((item) => item.id === id);
      const selectedPermissions = follow
        ? (permissionDrafts[String(id)] ?? defaultPermissionsForFinalidade(follow.finalidade))
        : [];
      if (!selectedPermissions.length) {
        setNotice({ kind: "error", text: "Selecione ao menos uma permissão para compartilhar." });
        return;
      }
      path = "/acompanhamentos/" + id + "/autorizar?" + queryString({ usuarioId: workspace.id });
      body = JSON.stringify({ permissoes: selectedPermissions });
    } else {
      setConfirmation({ kind: "encerrar", id });
      setConfirmationReason("");
      return;
    }
    await runAction(
      () =>
        apiRequest(path, {
          method: "PUT",
          ...(body ? { body } : {}),
        }),
      action === "aceitar"
        ? "Acompanhamento aceito."
        : action === "recusar"
          ? "Solicitação recusada."
          : action === "autorizar"
            ? "Permissões concedidas."
            : "Acompanhamento encerrado.",
    );
  }

  async function createPublication(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!workspace) return;
    const editing = editingPublicationId !== null;
    const path = editing
      ? "/publicacoes/" + editingPublicationId + "?" + queryString({ profissionalId: workspace.id })
      : "/publicacoes?" + queryString({ profissionalId: workspace.id });
    const succeeded = await runAction(
      () => apiRequest(path, {
        method: editing ? "PUT" : "POST",
        body: JSON.stringify({
          titulo: publicationTitle.trim(),
          conteudo: publicationContent.trim(),
        }),
      }),
      editing ? "Publicação atualizada." : "Publicação criada.",
    );
    if (succeeded) {
      setPublicationTitle("");
      setPublicationContent("");
      setEditingPublicationId(null);
    }
  }

  function editPublication(post: Publicacao) {
    setEditingPublicationId(post.id);
    setPublicationTitle(post.titulo);
    setPublicationContent(post.conteudo);
    setNotice({ kind: "info", text: "Editando a publicação #" + post.id + "." });
  }

  async function deactivatePublication(id: number) {
    setConfirmation({ kind: "inativarPublicacao", id });
    setConfirmationReason("");
  }

  async function submitConfirmation(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!workspace || !confirmation) return;
    const needsReason = confirmation.kind === "recusar" ||
      confirmation.kind === "encerrar" ||
      confirmation.kind === "inativarPublicacao";
    const reason = confirmationReason.trim();
    if (needsReason && !reason) {
      setNotice({ kind: "error", text: "Informe o motivo para continuar." });
      return;
    }

    let path = "";
    let method: "PUT" | "DELETE" = "PUT";
    let body: string | undefined;

    if (confirmation.kind === "recusar") {
      path = "/acompanhamentos/" + confirmation.id + "/recusar?" + queryString({ profissionalId: workspace.id });
      body = JSON.stringify({ motivo: reason });
    } else if (confirmation.kind === "encerrar") {
      path = "/acompanhamentos/" + confirmation.id + "/encerrar?" + queryString({ autorId: workspace.id });
      body = JSON.stringify({ motivo: reason });
    } else if (confirmation.kind === "inativarPublicacao") {
      path = "/publicacoes/" + confirmation.id + "?" + queryString({ responsavelId: workspace.id });
      method = "DELETE";
      body = JSON.stringify({ motivo: reason });
    } else if (confirmation.kind === "inativarRegistro") {
      path = "/usuarios/" + workspace.id + "/registros/" + confirmation.id;
      method = "DELETE";
    } else {
      path = "/planos/" + confirmation.id + "?" + queryString({ autorId: workspace.id });
      method = "DELETE";
    }

    const successMessage = confirmation.kind === "recusar"
      ? "Solicitação recusada."
      : confirmation.kind === "encerrar"
        ? "Acompanhamento encerrado."
        : confirmation.kind === "inativarPublicacao"
          ? "Publicação inativada."
          : confirmation.kind === "inativarRegistro"
            ? "Registro inativado."
            : "Plano inativado.";
    const succeeded = await runAction(
      () => apiRequest(path, { method, ...(body ? { body } : {}) }),
      successMessage,
    );
    if (succeeded) {
      setConfirmation(null);
      setConfirmationReason("");
    }
  }

  async function sendMessage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!workspace || !selectedFollowId || !messageDraft.trim()) return;
    const follow = followups.find((item) => String(item.id) === selectedFollowId);
    if (!follow || follow.status !== "ATIVO") {
      setNotice({ kind: "error", text: "Selecione um acompanhamento ativo para enviar uma interação." });
      return;
    }

    const isProfessionalUser = workspace.role === "NUTRICIONISTA" || workspace.role === "EDUCACAO_FISICA";
    if (!isProfessionalUser && interactionKind !== "MENSAGEM") {
      setNotice({ kind: "error", text: "Usuários podem enviar mensagens neste espaço." });
      return;
    }

    const requiredPermission = interactionKind === "OBSERVACAO"
      ? "INTERACAO:OBSERVACAO"
      : interactionKind === "META" ? "INTERACAO:META" : null;
    if (requiredPermission && !follow.permissoesConcedidas?.includes(requiredPermission)) {
      setNotice({ kind: "error", text: "O usuário não concedeu a permissão necessária para esta interação." });
      return;
    }

    if (interactionKind === "META" && !interactionSituation.trim()) {
      setNotice({ kind: "error", text: "Informe a situação inicial da meta." });
      return;
    }

    const recipient = interactionKind === "MENSAGEM"
      ? (workspace.role === "COMUM" ? follow.profissionalId : follow.usuarioId)
      : undefined;
    const params = queryString({
      acompanhamentoId: follow.id,
      autorId: workspace.id,
      destinatarioId: recipient,
    });
    const payload = {
      tipo: interactionKind,
      conteudo: messageDraft.trim(),
      prazo: interactionKind === "META" && interactionDeadline ? interactionDeadline : null,
      situacaoMeta: interactionKind === "META" ? interactionSituation : null,
    };
    const succeeded = await runAction(
      () => apiRequest("/interacoes?" + params, {
        method: "POST",
        body: JSON.stringify(payload),
      }),
      interactionKind === "MENSAGEM" ? "Mensagem enviada."
        : interactionKind === "OBSERVACAO" ? "Observação registrada."
          : "Meta registrada.",
    );
    if (succeeded) {
      setMessageDraft("");
      setInteractionDeadline("");
      setInteractionSituation("PENDENTE");
    }
  }

  function editInteraction(item: Interacao) {
    setInteractionToEdit(item);
    setInteractionEditContent(item.conteudo);
    setInteractionEditDeadline(item.prazo?.slice(0, 16) ?? "");
  }

  async function saveInteractionEdit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!workspace || !interactionToEdit) return;
    const succeeded = await runAction(
      () => apiRequest(
        "/interacoes/" + interactionToEdit.id + "?" + queryString({ usuarioId: workspace.id }),
        {
          method: "PUT",
          body: JSON.stringify({
            conteudo: interactionEditContent.trim(),
            prazo: interactionToEdit.tipo === "META" && interactionEditDeadline
              ? interactionEditDeadline
              : null,
          }),
        },
      ),
      "Interação atualizada.",
    );
    if (succeeded) {
      setInteractionToEdit(null);
      setInteractionEditContent("");
      setInteractionEditDeadline("");
    }
  }

  async function updateMetaSituation(interactionId: number, situation: string) {
    if (!workspace) return;
    await runAction(
      () => apiRequest(
        "/interacoes/" + interactionId + "/situacao-meta?" + queryString({ usuarioId: workspace.id }),
        { method: "PATCH", body: JSON.stringify({ situacao: situation }) },
      ),
      "Situação da meta atualizada.",
    );
  }

  async function markRead(interactionId: number) {
    if (!workspace) return;
    await runAction(
      () =>
        apiRequest(
          "/interacoes/" +
            interactionId +
            "/lida?" +
            queryString({ destinatarioId: workspace.id }),
          { method: "PATCH" },
        ),
      "Mensagem marcada como lida.",
    );
  }

  function logoutDemo() {
    setWorkspace(null);
    setView("inicio");
    setNotice(null);
    setRecords([]);
    setPlans([]);
    setFollowups([]);
    setPublications([]);
    setInteractions([]);
    setSelectedFollowId("");
    setConfirmation(null);
    setInteractionToEdit(null);
  }

  if (!workspace) {
    return (
      <main className="welcome-page">
        <a className="skip-link" href="#main-content">Pular para o formulário</a>
        <div className="welcome-glow welcome-glow-one" />
        <div className="welcome-glow welcome-glow-two" />
        <header className="welcome-top">
          <div className="brand-lockup">
            <div className="brand-mark"><Glyph name="activity" size={22} /></div>
            <span>Nutri<span>Follow</span></span>
          </div>
          <span className="top-caption">SAÚDE • ROTINA • EVOLUÇÃO</span>
        </header>

        <section className="welcome-grid">
          <div className="welcome-copy">
            <div className="eyebrow"><span className="eyebrow-dot" /> SUA JORNADA, MAIS ORGANIZADA</div>
            <h1>O cuidado com você não precisa ser <em>complicado.</em></h1>
            <p>
              Reúna registros, planos e acompanhamento profissional em uma
              experiência simples, clara e conectada.
            </p>
            <div className="welcome-points">
              <div><span><Glyph name="check" size={15} /></span> Registre sua evolução</div>
              <div><span><Glyph name="check" size={15} /></span> Acompanhe seus planos</div>
              <div><span><Glyph name="check" size={15} /></span> Converse com profissionais</div>
            </div>
            <div className="welcome-note">
              <span className="note-symbol">✳</span>
              <span>Um espaço para acompanhar hábitos e construir constância.</span>
            </div>
          </div>

          <section className="entry-card" id="main-content" tabIndex={-1}>
            <div className="entry-card-head">
              <div className="entry-kicker">BEM-VINDO AO NUTRIFOLLOW</div>
              <h2>{entryMode === "register" ? "Comece por aqui" : "Acessar demonstração"}</h2>
              <p>
                {entryMode === "register"
                  ? "Crie sua conta para começar a registrar sua evolução."
                  : "Use o ID de uma conta existente para testar os fluxos da API."}
              </p>
            </div>
            <div className="entry-tabs">
              <button
                type="button"
                className={entryMode === "register" ? "entry-tab active" : "entry-tab"}
                onClick={() => { setEntryMode("register"); setNotice(null); }}
              >
                Criar conta
              </button>
              <button
                type="button"
                className={entryMode === "demo" ? "entry-tab active" : "entry-tab"}
                onClick={() => { setEntryMode("demo"); setNotice(null); }}
              >
                Acessar por ID
              </button>
            </div>

            {entryMode === "register" ? (
              <form className="form-stack" onSubmit={register}>
                <Field label="Nome completo">
                  <input
                    value={registerName}
                    onChange={(event) => setRegisterName(event.target.value)}
                    placeholder="Como podemos chamar você?"
                    maxLength={100}
                    required
                  />
                </Field>
                <Field label="E-mail">
                  <input
                    type="email"
                    value={registerEmail}
                    onChange={(event) => setRegisterEmail(event.target.value)}
                    placeholder="voce@email.com"
                    maxLength={150}
                    required
                  />
                </Field>
                <Field label="Senha" hint="Mínimo de 8 caracteres.">
                  <input
                    type="password"
                    value={registerPassword}
                    onChange={(event) => setRegisterPassword(event.target.value)}
                    placeholder="Crie uma senha"
                    minLength={8}
                    maxLength={72}
                    required
                  />
                </Field>
                <button className="primary-button full-button" type="submit" disabled={busy}>
                  {busy ? "Criando conta..." : "Criar minha conta"}
                  <Glyph name="arrow" size={17} />
                </button>
              </form>
            ) : (
              <form className="form-stack" onSubmit={enterDemo}>
                <Field label="ID do usuário" hint="Identificador numérico existente no banco.">
                  <input
                    inputMode="numeric"
                    value={demoId}
                    onChange={(event) => setDemoId(event.target.value)}
                    placeholder="Ex.: 1"
                    required
                  />
                </Field>
                <Field label="Perfil para demonstração">
                  <select
                    value={demoRole}
                    onChange={(event) => setDemoRole(event.target.value as Role)}
                  >
                    <option value="COMUM">Usuário comum</option>
                    <option value="NUTRICIONISTA">Nutricionista</option>
                    <option value="EDUCACAO_FISICA">Profissional de Educação Física</option>
                    <option value="ADMINISTRADOR">Administrador (prévia)</option>
                  </select>
                </Field>
                <div className="inline-warning">
                  <Glyph name="warning" size={17} />
                  <span>
                    Demonstração técnica: o backend atual ainda não possui
                    endpoints de autenticação. Este acesso não valida senha nem
                    cria uma sessão segura.
                  </span>
                </div>
                <button className="primary-button full-button" type="submit">
                  Entrar no ambiente de demonstração
                  <Glyph name="arrow" size={17} />
                </button>
              </form>
            )}

            {notice ? (
              <div className={"notice " + notice.kind} role={notice.kind === "error" ? "alert" : "status"} aria-live={notice.kind === "error" ? "assertive" : "polite"}>
                {notice.text}
              </div>
            ) : null}
            <div className="entry-foot">
              <span>Persistência via API REST</span>
              <span className="api-indicator"><i /> Spring Boot</span>
            </div>
          </section>
        </section>

        <footer className="welcome-footer">
          <span>NutriFollow · Projeto acadêmico</span>
          <span>Nutrição e atividade física em um só lugar</span>
        </footer>
      </main>
    );
  }

  const activeTitle = navItems.find((item) => item.id === view)?.label ?? "NutriFollow";
  const activeFollow = followups.find(
    (item) => String(item.id) === selectedFollowId,
  );

  return (
    <main className="app-shell">
      <a className="skip-link" href="#main-content">Pular para o conteúdo</a>
      <aside className="sidebar">
        <div className="brand-lockup sidebar-brand">
          <div className="brand-mark"><Glyph name="activity" size={21} /></div>
          <span>Nutri<span>Follow</span></span>
        </div>

        <div className="sidebar-label">ESPAÇO DE TRABALHO</div>
        <nav className="side-nav" aria-label="Navegação principal">
          {navItems.map((item) => (
            <button
              type="button"
              key={item.id}
              onClick={() => { setView(item.id); setNotice(null); }}
              className={view === item.id ? "nav-link selected" : "nav-link"}
              aria-current={view === item.id ? "page" : undefined}
            >
              <Glyph name={item.icon} size={18} />
              <span>{item.label}</span>
              {view === item.id ? <span className="nav-active-dot" /> : null}
            </button>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="sidebar-status">
            <span className="status-pulse" />
            Ambiente de demonstração
          </div>
          <div className="profile-mini">
            <div className="avatar">{workspace.name.slice(0, 1).toUpperCase()}</div>
            <div className="profile-mini-copy">
              <strong>{workspace.name}</strong>
              <span>{roleLabel(workspace.role)} · ID {workspace.id}</span>
            </div>
          </div>
          <button type="button" className="logout-button" onClick={logoutDemo}>
            <Glyph name="logout" size={17} />
            Sair do ambiente
          </button>
        </div>
      </aside>

      <section className="main-area" id="main-content" tabIndex={-1}>
        <header className="topbar">
          <div className="breadcrumbs">
            <span>NutriFollow</span>
            <b>/</b>
            <strong>{activeTitle}</strong>
          </div>
          <div className="topbar-actions">
            <span className="topbar-role">{roleLabel(workspace.role)}</span>
            <button
              type="button"
              className="icon-button"
              title="Atualizar dados"
              aria-label="Atualizar dados"
              onClick={() => setReloadKey((value) => value + 1)}
            >
              <Glyph name="refresh" />
            </button>
            <div className="avatar top-avatar">{workspace.name.slice(0, 1).toUpperCase()}</div>
          </div>
        </header>

        <div className="content-wrap">
          <div className="demo-banner">
            <Glyph name="warning" size={17} />
            <span>
              <strong>Modo de demonstração.</strong> As ações usam o ID informado e
              as regras do backend, mas ainda não existe login/sessão autenticada.
            </span>
          </div>

          {notice ? (
            <div className={"notice page-notice " + notice.kind} role="status">
              {notice.text}
              <button type="button" onClick={() => setNotice(null)} aria-label="Fechar aviso">×</button>
            </div>
          ) : null}

          {loading ? <div className="loading-line" role="status" aria-live="polite"><span /> Carregando dados do servidor...</div> : null}

          {view === "inicio" ? (
            <>
              <div className="page-heading">
                <div>
                  <div className="eyebrow dark-eyebrow">SEU PAINEL</div>
                  <h1>Olá, {workspace.name.split(" ")[0]}.</h1>
                  <p>Veja o que está acontecendo na sua rotina hoje.</p>
                </div>
                <div className="date-chip"><Glyph name="clock" size={16} /> Dados atualizados sob demanda</div>
              </div>

              {workspace.role === "ADMINISTRADOR" ? (
                <section className="panel empty-admin">
                  <div className="panel-icon"><Glyph name="shield" size={24} /></div>
                  <h2>Área administrativa</h2>
                  <p>
                    O backend atual ainda não expõe Controllers para listar
                    usuários, verificar profissionais ou moderar contas. Esta
                    área não simula operações administrativas que não existem na API.
                  </p>
                  <button className="secondary-button" onClick={() => setView("administracao")}>
                    Ver pendências da API <Glyph name="arrow" size={16} />
                  </button>
                </section>
              ) : (
                <>
                  <div className="stats-grid">
                    {isCommon ? (
                      <>
                        <StatCard label="Treinos hoje" value={todayRecords.filter((item) => item.tipo === "TREINO").length} icon="activity" tone="mint" detail="Registros de treino" />
                        <StatCard label="Refeições hoje" value={todayRecords.filter((item) => item.tipo === "REFEICAO").length} icon="book" tone="blue" detail="Registros alimentares" />
                        <StatCard label="Água hoje" value={(todayRecords.filter((item) => item.tipo === "AGUA").reduce((total, item) => total + Number(item.quantidadeMl ?? 0), 0) / 1000).toLocaleString("pt-BR", { maximumFractionDigits: 1 }) + " L"} icon="activity" tone="mint" detail="Volume registrado" />
                        <StatCard label="Peso recente" value={latestWeightRecord?.pesoKg == null ? "—" : Number(latestWeightRecord.pesoKg).toLocaleString("pt-BR", { maximumFractionDigits: 1 }) + " kg"} icon="activity" tone="peach" detail={latestWeightRecord ? formatDate(latestWeightRecord.registradoEm) : "Sem registro"} />
                        <StatCard label="Vínculos ativos" value={followups.filter((item) => item.status === "ATIVO").length} icon="users" tone="blue" detail="Acompanhamentos ativos" />
                        <StatCard label="Planos ativos" value={plans.filter((item) => item.ativo).length} icon="book" tone="mint" detail="Treinos e orientações" />
                      </>
                    ) : (
                      <>
                        <StatCard label="Acompanhamentos" value={followups.length} icon="users" tone="mint" detail="Solicitações e vínculos" />
                        <StatCard label="Ativos" value={followups.filter((item) => item.status === "ATIVO").length} icon="activity" tone="blue" detail="Acompanhamentos em andamento" />
                        <StatCard label="Pendentes" value={followups.filter((item) => item.status === "PENDENTE").length} icon="clock" tone="peach" detail="Aguardando resposta" />
                        <StatCard label="Publicações" value={publications.length} icon="heart" tone="mint" detail="Conteúdos disponíveis" />
                      </>
                    )}
                  </div>

                  <div className="dashboard-grid">
                    <section className="panel panel-large">
                      <div className="panel-heading">
                        <div>
                          <span className="section-kicker">ACOMPANHAMENTO</span>
                          <h2>{isCommon ? "Seus acompanhamentos" : "Solicitações recentes"}</h2>
                        </div>
                        <button className="text-button" onClick={() => setView("acompanhamentos")}>
                          Ver todos <Glyph name="arrow" size={15} />
                        </button>
                      </div>
                      {followups.length === 0 ? (
                        <EmptyState text="Ainda não há acompanhamentos para exibir." />
                      ) : (
                        <div className="simple-list">
                          {followups.slice(0, 4).map((item) => (
                            <div className="list-row" key={item.id}>
                              <div className="list-icon"><Glyph name="users" size={17} /></div>
                              <div className="list-copy">
                                <strong>{statusLabel(item.finalidade)}</strong>
                                <span>Solicitado em {formatDate(item.solicitadoEm)}</span>
                              </div>
                              <StatusPill value={item.status} />
                            </div>
                          ))}
                        </div>
                      )}
                    </section>

                    <section className="panel">
                      <div className="panel-heading">
                        <div>
                          <span className="section-kicker">ATALHOS</span>
                          <h2>Acesso rápido</h2>
                        </div>
                      </div>
                      <div className="quick-actions">
                        {isCommon ? (
                          <>
                            <QuickAction icon="activity" title="Novo registro" text="Anote uma atividade" onClick={() => setView("registros")} />
                            <QuickAction icon="book" title="Ver planos" text="Consulte suas rotinas" onClick={() => setView("planos")} />
                          </>
                        ) : (
                          <>
                            <QuickAction icon="users" title="Pacientes" text="Gerencie solicitações" onClick={() => setView("acompanhamentos")} />
                            <QuickAction icon="heart" title="Publicar conteúdo" text="Compartilhe orientação" onClick={() => setView("comunidade")} />
                          </>
                        )}
                        <QuickAction icon="message" title="Conversas" text="Fale com seu vínculo" onClick={() => setView("mensagens")} />
                      </div>
                    </section>

                    <section className="panel panel-large">
                      <div className="panel-heading">
                        <div>
                          <span className="section-kicker">COMUNIDADE</span>
                          <h2>Conteúdos recentes</h2>
                        </div>
                        <button className="text-button" onClick={() => setView("comunidade")}>
                          Explorar <Glyph name="arrow" size={15} />
                        </button>
                      </div>
                      {publications.length === 0 ? (
                        <EmptyState text="Nenhuma publicação disponível no momento." />
                      ) : (
                        <div className="publication-grid compact-publications">
                          {publications.slice(0, 2).map((post) => (
                            <article className="publication-card" key={post.id}>
                              <div className="post-tag">CONTEÚDO PROFISSIONAL</div>
                              <h3>{post.titulo}</h3>
                              <p>{post.conteudo}</p>
                              <span>Publicado em {formatDate(post.publicadaEm)}</span>
                            </article>
                          ))}
                        </div>
                      )}
                    </section>
                  </div>
                </>
              )}
            </>
          ) : null}

          {view === "registros" ? (
            <>
              <PageHeading eyebrow="HISTÓRICO PESSOAL" title="Meus registros" subtitle="Anote o essencial da sua rotina sem transformar cada registro em um formulário longo." />
              {isCommon ? (
                <div className="two-column">
                  <section className="panel">
                    <PanelTitle
                      title={editingRecordId === null ? "Novo registro" : "Editar registro"}
                      subtitle={editingRecordId === null ? "Escolha o tipo e preencha somente os dados necessários." : "O tipo original será preservado; os demais dados podem ser corrigidos."}
                    />
                    <form className="form-stack" onSubmit={createRecord}>
                      <Field label="Tipo de registro">
                        <select value={recordDraft.tipo} disabled={editingRecordId !== null} onChange={(event) => setRecordDraft({ ...recordDraft, tipo: event.target.value })}>
                          <option value="TREINO">Treino</option>
                          <option value="REFEICAO">Refeição</option>
                          <option value="AGUA">Água</option>
                          <option value="PESO">Peso corporal</option>
                        </select>
                      </Field>
                      <Field label="Descrição">
                        <textarea value={recordDraft.descricao} onChange={(event) => setRecordDraft({ ...recordDraft, descricao: event.target.value })} maxLength={2000} rows={3} placeholder={recordDraft.tipo === "TREINO" ? "Ex.: Supino reto" : recordDraft.tipo === "REFEICAO" ? "Ex.: Arroz, feijão, frango e salada" : recordDraft.tipo === "AGUA" ? "Ex.: Copo de água após o treino" : "Ex.: Pesagem pela manhã"} required />
                      </Field>
                      {recordDraft.tipo === "TREINO" ? (
                        <div className="form-grid three-fields">
                          <Field label="Séries"><input type="number" min="1" step="1" value={recordDraft.series} onChange={(event) => setRecordDraft({ ...recordDraft, series: event.target.value })} required /></Field>
                          <Field label="Repetições"><input type="number" min="1" step="1" value={recordDraft.repeticoes} onChange={(event) => setRecordDraft({ ...recordDraft, repeticoes: event.target.value })} required /></Field>
                          <Field label="Carga (kg)"><input type="number" min="0" step="0.1" value={recordDraft.cargaKg} onChange={(event) => setRecordDraft({ ...recordDraft, cargaKg: event.target.value })} required /></Field>
                        </div>
                      ) : null}
                      {recordDraft.tipo === "REFEICAO" ? (
                        <Field label="Tipo de refeição">
                          <select value={recordDraft.tipoRefeicao} onChange={(event) => setRecordDraft({ ...recordDraft, tipoRefeicao: event.target.value })}>
                            <option value="CAFE_DA_MANHA">Café da manhã</option>
                            <option value="LANCHE">Lanche</option>
                            <option value="ALMOCO">Almoço</option>
                            <option value="JANTAR">Jantar</option>
                            <option value="CEIA">Ceia</option>
                          </select>
                        </Field>
                      ) : null}
                      {recordDraft.tipo === "AGUA" ? (
                        <Field label="Quantidade (ml)"><input type="number" min="1" step="1" value={recordDraft.quantidadeMl} onChange={(event) => setRecordDraft({ ...recordDraft, quantidadeMl: event.target.value })} required /></Field>
                      ) : null}
                      {recordDraft.tipo === "PESO" ? (
                        <Field label="Peso corporal (kg)"><input type="number" min="0.1" step="0.1" value={recordDraft.pesoKg} onChange={(event) => setRecordDraft({ ...recordDraft, pesoKg: event.target.value })} required /></Field>
                      ) : null}
                      <Field label="Observações (opcional)">
                        <textarea value={recordDraft.observacoes} onChange={(event) => setRecordDraft({ ...recordDraft, observacoes: event.target.value })} maxLength={2000} rows={2} placeholder="Detalhes adicionais..." />
                      </Field>
                      <div className="button-row">
                        <button className="primary-button" type="submit" disabled={busy}>{busy ? "Salvando..." : editingRecordId === null ? "Salvar registro" : "Salvar alterações"} <Glyph name="arrow" size={16} /></button>
                        {editingRecordId !== null ? <button className="secondary-button" type="button" onClick={cancelRecordEdit} disabled={busy}>Cancelar edição</button> : null}
                      </div>
                    </form>
                  </section>
                  <section className="panel">
                    <PanelTitle title="Histórico" subtitle={records.length + " registro(s) ativo(s)"} />
                    {records.length === 0 ? <EmptyState text="Você ainda não possui registros ativos." /> : (
                      <div className="timeline-list">
                        {records.map((item) => (
                          <article className="timeline-item" key={item.id}>
                            <div className="timeline-marker" />
                            <div className="timeline-content">
                              <div className="timeline-top">
                                <StatusPill value={item.tipo} />
                                <span>{formatDate(item.registradoEm)}</span>
                              </div>
                              <h3>{item.descricao}</h3>
                              <p>{recordDetail(item)}</p>
                              {item.observacoes ? <p className="muted-line">{item.observacoes}</p> : null}
                              <div className="button-row">
                                <button type="button" className="text-button" onClick={() => editRecord(item)}>Editar</button>
                                <button type="button" className="danger-text-button" onClick={() => setConfirmation({ kind: "inativarRegistro", id: item.id })}>Inativar</button>
                              </div>
                            </div>
                          </article>
                        ))}
                      </div>
                    )}
                  </section>
                </div>
              ) : <InfoPanel text="Registros pessoais são disponibilizados para o perfil de usuário comum." />}
            </>
          ) : null}

          {view === "planos" ? (
            <>
              <PageHeading
                eyebrow="ROTINAS E ORIENTAÇÕES"
                title={isCommon ? "Meus planos" : "Planos dos acompanhamentos"}
                subtitle={isCommon ? "Crie sua rotina e ajuste os itens quando precisar." : "Crie orientações somente para acompanhamentos ativos da sua especialidade."}
              />
              <div className="two-column">
                <section className="panel">
                  <PanelTitle
                    title={editingPlanId === null ? "Novo plano" : "Editar plano"}
                    subtitle={editingPlanId === null
                      ? (isCommon ? "Monte seu próprio plano de treino, item por item." : "Selecione um acompanhamento ativo e autorizado.")
                      : "A edição preserva a quantidade e a ordem dos itens ativos existentes."}
                  />
                  <form className="form-stack" onSubmit={createPlan}>
                    <Field label="Tipo de plano">
                      <select
                        value={planKind}
                        disabled={editingPlanId !== null}
                        onChange={(event) => { setPlanKind(event.target.value as "TREINO" | "ALIMENTAR"); setPlanItems([]); setEditingPlanItemIndex(null); }}
                      >
                        {isCommon ? <option value="TREINO">Treino próprio</option> : null}
                        {workspace.role === "NUTRICIONISTA" ? <option value="ALIMENTAR">Plano alimentar</option> : null}
                        {workspace.role === "EDUCACAO_FISICA" ? <option value="TREINO">Plano de treino</option> : null}
                      </select>
                    </Field>

                    {editingPlanId === null && (isCommon || planKind === "TREINO") ? (
                      <Field label="Título do plano">
                        <input value={planTitle} onChange={(event) => setPlanTitle(event.target.value)} maxLength={255} placeholder="Ex.: Treino A — membros superiores" required />
                      </Field>
                    ) : null}

                    {isProfessional ? (
                      <Field label="Acompanhamento ativo">
                        <select value={planFollowId} onChange={(event) => setPlanFollowId(event.target.value)} required>
                          <option value="">Selecione o acompanhamento</option>
                          {eligiblePlanFollowups.map((item) => (
                            <option key={item.id} value={item.id}>
                              Acompanhamento #{item.id} · Usuário #{item.usuarioId}
                            </option>
                          ))}
                        </select>
                        {eligiblePlanFollowups.length === 0 ? (
                          <span className="field-hint">Não há acompanhamento ativo da especialidade correspondente disponível neste momento.</span>
                        ) : (
                          <span className="field-hint">Somente vínculos ativos da sua especialidade são exibidos.</span>
                        )}
                      </Field>
                    ) : null}

                    {editingPlanId !== null ? (
                      <div className="inline-warning">
                        <Glyph name="warning" size={16} />
                        <span>A API atual só permite substituir os itens ativos um a um. Para manter o histórico, não é possível adicionar ou remover itens durante esta edição.</span>
                      </div>
                    ) : null}

                    <div className="form-divider"><span>{editingPlanItemIndex === null ? "ITEM DO PLANO" : "EDITANDO ITEM " + (editingPlanItemIndex + 1)}</span></div>
                    <Field label="Título do item">
                      <input value={itemDraft.titulo} onChange={(event) => setItemDraft({ ...itemDraft, titulo: event.target.value })} maxLength={255} placeholder={planKind === "TREINO" ? "Ex.: Agachamento" : "Ex.: Café da manhã"} />
                    </Field>

                    {planKind === "TREINO" ? (
                      <>
                        <Field label="Grupo muscular">
                          <input value={itemDraft.grupoMuscular} onChange={(event) => setItemDraft({ ...itemDraft, grupoMuscular: event.target.value })} maxLength={255} placeholder="Ex.: Quadríceps" />
                        </Field>
                        <div className="form-grid three-fields">
                          <Field label="Séries"><input type="number" min="1" step="1" value={itemDraft.seriesPrevistas} onChange={(event) => setItemDraft({ ...itemDraft, seriesPrevistas: event.target.value })} /></Field>
                          <Field label="Repetições"><input type="number" min="1" step="1" value={itemDraft.repeticoesPrevistas} onChange={(event) => setItemDraft({ ...itemDraft, repeticoesPrevistas: event.target.value })} /></Field>
                          <Field label="Carga (kg)"><input type="number" min="0" step="0.1" value={itemDraft.cargaPrevistaKg} onChange={(event) => setItemDraft({ ...itemDraft, cargaPrevistaKg: event.target.value })} /></Field>
                        </div>
                        <div className="form-grid two-fields">
                          <Field label="Descanso (seg.)"><input type="number" min="0" step="1" value={itemDraft.descansoSegundos} onChange={(event) => setItemDraft({ ...itemDraft, descansoSegundos: event.target.value })} /></Field>
                          <Field label="Horário sugerido"><input type="time" value={itemDraft.horarioSugerido} onChange={(event) => setItemDraft({ ...itemDraft, horarioSugerido: event.target.value })} /></Field>
                        </div>
                      </>
                    ) : (
                      <>
                        <Field label="Orientação alimentar">
                          <textarea value={itemDraft.descricao} onChange={(event) => setItemDraft({ ...itemDraft, descricao: event.target.value })} maxLength={2000} rows={3} placeholder="Descreva alimentos, porções ou recomendações..." />
                        </Field>
                        <Field label="Horário sugerido"><input type="time" value={itemDraft.horarioSugerido} onChange={(event) => setItemDraft({ ...itemDraft, horarioSugerido: event.target.value })} /></Field>
                      </>
                    )}

                    <div className="button-row">
                      <button type="button" className="secondary-button" onClick={addPlanItem} disabled={editingPlanId !== null && editingPlanItemIndex === null}>
                        <Glyph name={editingPlanItemIndex === null ? "plus" : "check"} size={16} />
                        {editingPlanItemIndex === null ? "Adicionar item" : "Atualizar item"}
                      </button>
                      {editingPlanItemIndex !== null ? <button type="button" className="text-button" onClick={() => { setEditingPlanItemIndex(null); setItemDraft(emptyPlanItem); }}>Cancelar edição do item</button> : null}
                    </div>

                    {planItems.length > 0 ? (
                      <div className="draft-items">
                        <strong>{planItems.length} item(ns) {editingPlanId === null ? "no rascunho" : "mantidos no plano"}</strong>
                        {planItems.map((item, index) => (
                          <div className="draft-item" key={index}>
                            <span>{String(item.titulo)}</span>
                            <div className="draft-item-actions">
                              <button type="button" onClick={() => editPlanItem(index)}>Editar</button>
                              {editingPlanId === null ? (
                                <button type="button" onClick={() => setPlanItems((current) => current.filter((_, itemIndex) => itemIndex !== index).map((value, itemIndex) => ({ ...value, ordem: itemIndex + 1 })))} aria-label="Remover item">Remover</button>
                              ) : null}
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : null}

                    <button className="primary-button" type="submit" disabled={busy || planItems.length === 0 || (editingPlanItemIndex !== null) || (isProfessional && eligiblePlanFollowups.length === 0)}>
                      {busy ? "Salvando..." : editingPlanId === null ? "Criar plano" : "Salvar alterações"}
                      <Glyph name="arrow" size={16} />
                    </button>
                    {editingPlanId !== null ? <button className="secondary-button" type="button" onClick={cancelPlanEdit} disabled={busy}>Cancelar edição do plano</button> : null}
                  </form>
                </section>

                <section className="panel">
                  <PanelTitle
                    title={isCommon ? "Planos cadastrados" : "Planos criados nesta sessão"}
                    subtitle={isCommon ? plans.length + " plano(s) ativo(s)" : "A listagem segura de planos dos pacientes ainda não está disponível na API."}
                  />
                  {isProfessional ? (
                    <div className="inline-warning plan-api-note">
                      <Glyph name="shield" size={16} />
                      <span>Por privacidade, a tela não consulta planos de pacientes apenas pelo ID. A API atual ainda não oferece uma listagem que confirme o vínculo ativo e as permissões antes de devolver esses dados.</span>
                    </div>
                  ) : null}
                  {plans.length === 0 ? <EmptyState text={isProfessional ? "Nenhum plano foi criado nesta sessão." : "Nenhum plano ativo foi encontrado."} /> : (
                    <div className="plan-list">
                      {plans.map((plan) => {
                        const canEdit = plan.ativo && (isCommon || (isProfessional && followups.some((item) => item.status === "ATIVO" && item.profissionalId === workspace.id && item.usuarioId === plan.usuarioId && item.finalidade === (plan.tipo === "ALIMENTAR" ? "NUTRICAO" : "EDUCACAO_FISICA") && (item.permissoesConcedidas ?? []).includes(plan.tipo + ":ALTERAR"))));
                        return (
                          <article className="plan-card" key={plan.id}>
                            <div className="plan-card-top">
                              <StatusPill value={plan.tipo} />
                              <span>Versão {plan.versao ?? 1}</span>
                            </div>
                            <h3>{plan.titulo}</h3>
                            {plan.objetivo ? <p>{plan.objetivo}</p> : null}
                            <div className="plan-items">
                              {(plan.itens ?? []).filter((item) => item.ativo !== false).map((item, index) => (
                                <div className="plan-item" key={item.id ?? index}>
                                  <span className="plan-item-number">{index + 1}</span>
                                  <div>
                                    <strong>{item.titulo}</strong>
                                    <small>{plan.tipo === "TREINO" ? (item.seriesPrevistas ?? "—") + " séries · " + (item.repeticoesPrevistas ?? "—") + " repetições" : item.descricao}</small>
                                  </div>
                                </div>
                              ))}
                            </div>
                            {plan.resumoUltimaAlteracao ? <p className="muted-line plan-change-summary">{plan.resumoUltimaAlteracao} · Atualizado em {formatDate(plan.atualizadoEm)}</p> : null}
                            <div className="plan-card-bottom">
                              <span>Criado em {formatDate(plan.criadoEm)}</span>
                              <div className="button-row">
                                {canEdit ? <button type="button" className="text-button" onClick={() => startEditingPlan(plan)}>Editar itens</button> : null}
                                {isCommon && plan.ativo ? <button type="button" className="danger-text-button" onClick={() => setConfirmation({ kind: "inativarPlano", id: plan.id })}>Inativar plano</button> : null}
                              </div>
                            </div>
                          </article>
                        );
                      })}
                    </div>
                  )}
                </section>
              </div>
            </>
          ) : null}

          {view === "acompanhamentos" ? (
            <>
              <PageHeading eyebrow={isCommon ? "CUIDADO COMPARTILHADO" : "GESTÃO DE PACIENTES"} title={isCommon ? "Acompanhamentos" : "Pacientes e solicitações"} subtitle={isCommon ? "Solicite acompanhamento e defina quais dados deseja compartilhar." : "Acompanhe solicitações e vínculos sob sua responsabilidade."} />
              <div className="two-column">
                {isCommon ? (
                  <section className="panel">
                    <PanelTitle title="Solicitar acompanhamento" subtitle="Informe o ID do profissional que já possui cadastro e verificação." />
                    <form className="form-stack" onSubmit={requestFollowup}>
                      <Field label="ID do profissional"><input inputMode="numeric" value={professionalId} onChange={(event) => setProfessionalId(event.target.value)} placeholder="ID informado pelo profissional" required /></Field>
                      <Field label="Finalidade">
                        <select value={finalidade} onChange={(event) => setFinalidade(event.target.value)}>
                          <option value="NUTRICAO">Nutrição</option>
                          <option value="EDUCACAO_FISICA">Educação física</option>
                        </select>
                      </Field>
                      <button className="primary-button" type="submit" disabled={busy}>Enviar solicitação <Glyph name="arrow" size={16} /></button>
                    </form>
                  </section>
                ) : (
                  <section className="panel">
                    <PanelTitle title="Visão profissional" subtitle="As solicitações são carregadas a partir do seu ID no backend." />
                    <div className="professional-tip">
                      <Glyph name="shield" size={20} />
                      <p>O backend verifica se você é profissional ativo e verificado e se o vínculo permite a operação solicitada.</p>
                    </div>
                    <div className="stats-grid mini-stats">
                      <StatCard label="Pendentes" value={followups.filter((item) => item.status === "PENDENTE").length} icon="clock" tone="peach" />
                      <StatCard label="Ativos" value={followups.filter((item) => item.status === "ATIVO").length} icon="check" tone="mint" />
                    </div>
                  </section>
                )}

                <section className="panel">
                  <PanelTitle title="Seus vínculos" subtitle={followups.length + " acompanhamento(s)"} />
                  {followups.length === 0 ? <EmptyState text="Nenhum acompanhamento encontrado para este ID." /> : (
                    <div className="follow-list">
                      {followups.map((item) => (
                        <article className="follow-card" key={item.id}>
                          <div className="follow-card-heading">
                            <div className="list-icon"><Glyph name="users" size={17} /></div>
                            <div className="list-copy">
                              <strong>{statusLabel(item.finalidade)}</strong>
                              <span>Solicitação #{item.id} · {formatDate(item.solicitadoEm)}</span>
                            </div>
                            <StatusPill value={item.status} />
                          </div>
                          <div className="follow-meta">
                            <span>Usuário #{item.usuarioId}</span>
                            <span>Profissional #{item.profissionalId}</span>
                          </div>
                          {item.motivoRecusa ? <p className="muted-copy">Motivo da recusa: {item.motivoRecusa}</p> : null}
                          {item.status === "PENDENTE" && isProfessional ? (
                            <div className="button-row">
                              <button className="primary-button small-button" onClick={() => followupAction(item.id, "aceitar")} disabled={busy}>Aceitar</button>
                              <button className="secondary-button small-button" onClick={() => followupAction(item.id, "recusar")} disabled={busy}>Recusar</button>
                            </div>
                          ) : null}
                          {item.status === "ACEITO" && isCommon ? (
                            <>
                              <div className="permission-consent">
                                <div className="section-kicker">ESCOLHA O QUE DESEJA COMPARTILHAR</div>
                                <p className="muted-copy">Libere somente as operações necessárias. Você poderá encerrar o acompanhamento depois.</p>
                                <div className="permission-list">
                                  {permissionsForFinalidade(item.finalidade).map((permission) => (
                                    <label key={permission} className="permission-option">
                                      <input
                                        type="checkbox"
                                        checked={selectedPermissionsFor(item).includes(permission)}
                                        onChange={(event) => toggleFollowupPermission(item, permission, event.target.checked)}
                                      />
                                      <span>{permission.replace(":", " · ")}</span>
                                    </label>
                                  ))}
                                </div>
                              </div>
                              <button className="primary-button small-button" onClick={() => followupAction(item.id, "autorizar")} disabled={busy || selectedPermissionsFor(item).length === 0}>Autorizar compartilhamento</button>
                            </>
                          ) : null}
                          {item.status === "ATIVO" ? (
                            <div className="follow-active-row">
                              {item.permissoesConcedidas?.length ? <span>{item.permissoesConcedidas.length} permissão(ões) concedida(s)</span> : null}
                              <button className="danger-text-button" onClick={() => followupAction(item.id, "encerrar")} disabled={busy}>Encerrar acompanhamento</button>
                            </div>
                          ) : null}
                        </article>
                      ))}
                    </div>
                  )}
                </section>
              </div>
            </>
          ) : null}

          {view === "comunidade" ? (
            <>
              <PageHeading eyebrow="CONTEÚDO E ORIENTAÇÃO" title={isProfessional ? "Publicações" : "Comunidade"} subtitle="Conteúdos publicados por profissionais verificados." />
              {isProfessional ? (
                <section className="panel publication-composer">
                  <PanelTitle title={editingPublicationId === null ? "Nova publicação" : "Editar publicação"} subtitle="A API permite publicar somente para profissionais ativos e verificados." />
                  <form className="form-stack" onSubmit={createPublication}>
                    <Field label="Título"><input value={publicationTitle} onChange={(event) => setPublicationTitle(event.target.value)} maxLength={255} required placeholder="Título do conteúdo" /></Field>
                    <Field label="Conteúdo"><textarea value={publicationContent} onChange={(event) => setPublicationContent(event.target.value)} maxLength={5000} rows={4} required placeholder="Compartilhe uma orientação ou conteúdo..." /></Field>
                    <div className="button-row">
                      <button className="primary-button" type="submit" disabled={busy}>{editingPublicationId === null ? "Publicar conteúdo" : "Salvar alterações"} <Glyph name="arrow" size={16} /></button>
                      {editingPublicationId !== null ? <button type="button" className="secondary-button" onClick={() => { setEditingPublicationId(null); setPublicationTitle(""); setPublicationContent(""); setNotice(null); }}>Cancelar edição</button> : null}
                    </div>
                  </form>
                </section>
              ) : null}
              {publications.length === 0 ? (
                <section className="panel"><EmptyState text="Ainda não há publicações disponíveis." /></section>
              ) : (
                <div className="publication-grid">
                  {publications.map((post) => (
                    <article className="publication-card" key={post.id}>
                      <div className="publication-art"><span>NF</span><i>✳</i></div>
                      <div className="post-tag">ORIENTAÇÃO PROFISSIONAL</div>
                      <h3>{post.titulo}</h3>
                      <p>{post.conteudo}</p>
                      <div className="publication-footer">
                        <span>Profissional #{post.autorId} · {formatDate(post.publicadaEm)}</span>
                        {isProfessional && post.autorId === workspace.id ? (
                          <div className="button-row">
                            <button type="button" className="text-button" onClick={() => editPublication(post)}>Editar</button>
                            <button type="button" className="danger-text-button" onClick={() => deactivatePublication(post.id)}>Inativar</button>
                          </div>
                        ) : null}
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </>
          ) : null}

          {view === "mensagens" ? (
            <>
              <PageHeading eyebrow="COMUNICAÇÃO" title="Conversas e acompanhamento" subtitle="Mensagens, observações e metas dentro dos vínculos ativos." />
              <section className="panel messages-panel">
                <div className="messages-toolbar">
                  <Field label="Acompanhamento">
                    <select
                      value={selectedFollowId}
                      onChange={(event) => { setSelectedFollowId(event.target.value); setInteractionKind("MENSAGEM"); }}
                    >
                      <option value="">Selecione um acompanhamento</option>
                      {followups.map((item) => (
                        <option value={item.id} key={item.id}>#{item.id} · {statusLabel(item.finalidade)} · {statusLabel(item.status)}</option>
                      ))}
                    </select>
                  </Field>
                  {selectedFollowId ? <span className="date-chip">Vínculo #{selectedFollowId}</span> : null}
                </div>
                {!activeFollow ? (
                  <EmptyState text="Selecione um acompanhamento para consultar as interações." />
                ) : (
                  <>
                    <div className="conversation-info">
                      <div className="avatar"><Glyph name="users" size={18} /></div>
                      <div><strong>{statusLabel(activeFollow.finalidade)}</strong><span>Usuário #{activeFollow.usuarioId} · Profissional #{activeFollow.profissionalId}</span></div>
                      <StatusPill value={activeFollow.status} />
                    </div>

                    <div className="message-list" aria-live="polite">
                      {interactions.length === 0 ? <EmptyState text="Nenhuma interação registrada neste acompanhamento." /> : interactions.map((item) => (
                        <article className={item.autorId === workspace.id ? "message-bubble own-message" : "message-bubble"} key={item.id}>
                          <div className="message-meta">
                            <strong>{item.tipo === "MENSAGEM" ? (item.autorId === workspace.id ? "Você" : "Participante") : statusLabel(item.tipo)}</strong>
                            <span>{formatDate(item.criadaEm)}</span>
                          </div>
                          <p>{item.conteudo}</p>
                          {item.tipo !== "MENSAGEM" && isProfessional && item.autorId === workspace.id && activeFollow.status === "ATIVO" && activeFollow.permissoesConcedidas?.includes(item.tipo === "META" ? "INTERACAO:ALTERAR_META" : "INTERACAO:ALTERAR_OBSERVACAO") ? (
                            <button type="button" className="text-button" onClick={() => editInteraction(item)}>Editar interação</button>
                          ) : null}
                          {item.tipo === "META" ? (
                            <div className="meta-details">
                              <span>Situação: <strong>{item.situacaoMeta ?? "Pendente"}</strong></span>
                              {item.prazo ? <span>Prazo: {formatDate(item.prazo)}</span> : null}
                              {isProfessional && item.autorId === workspace.id && activeFollow.status === "ATIVO" && activeFollow.permissoesConcedidas?.includes("INTERACAO:ALTERAR_META") ? (
                                <Field label="Atualizar situação da meta">
                                  <select value={item.situacaoMeta ?? "PENDENTE"} onChange={(event) => updateMetaSituation(item.id, event.target.value)}>
                                    <option value="PENDENTE">Pendente</option>
                                    <option value="EM_ANDAMENTO">Em andamento</option>
                                    <option value="CONCLUIDA">Concluída</option>
                                    <option value="CANCELADA">Cancelada</option>
                                  </select>
                                </Field>
                              ) : null}
                            </div>
                          ) : null}
                          {item.destinatarioId === workspace.id && !item.lidaEm ? (
                            <button type="button" className="text-button" onClick={() => markRead(item.id)}>Marcar como lida</button>
                          ) : item.tipo === "MENSAGEM" && item.destinatarioId === workspace.id ? (
                            <span className="message-read-state">Lida</span>
                          ) : null}
                        </article>
                      ))}
                    </div>

                    {activeFollow.status === "ATIVO" ? (
                      <form className="message-compose message-compose-extended" onSubmit={sendMessage}>
                        {isProfessional ? (
                          <Field label="Tipo de interação">
                            <select value={interactionKind} onChange={(event) => setInteractionKind(event.target.value as "MENSAGEM" | "OBSERVACAO" | "META")}>
                              <option value="MENSAGEM">Mensagem</option>
                              {activeFollow.permissoesConcedidas?.includes("INTERACAO:OBSERVACAO") ? <option value="OBSERVACAO">Observação profissional</option> : null}
                              {activeFollow.permissoesConcedidas?.includes("INTERACAO:META") ? <option value="META">Meta</option> : null}
                            </select>
                          </Field>
                        ) : null}
                        <Field label={interactionKind === "MENSAGEM" ? "Mensagem" : interactionKind === "OBSERVACAO" ? "Observação" : "Descrição da meta"}>
                          <input
                            value={messageDraft}
                            onChange={(event) => setMessageDraft(event.target.value)}
                            maxLength={2000}
                            placeholder={interactionKind === "MENSAGEM" ? "Escreva uma mensagem..." : interactionKind === "OBSERVACAO" ? "Registre uma observação do acompanhamento..." : "Descreva a meta combinada..."}
                            required
                          />
                        </Field>
                        {interactionKind === "META" ? (
                          <div className="form-grid two-fields meta-compose-fields">
                            <Field label="Prazo (opcional)"><input type="datetime-local" value={interactionDeadline} onChange={(event) => setInteractionDeadline(event.target.value)} /></Field>
                            <Field label="Situação inicial">
                              <select value={interactionSituation} onChange={(event) => setInteractionSituation(event.target.value)}>
                                <option value="PENDENTE">Pendente</option>
                                <option value="EM_ANDAMENTO">Em andamento</option>
                              </select>
                            </Field>
                          </div>
                        ) : null}
                        <button className="primary-button" type="submit" disabled={busy || !messageDraft.trim()}>
                          {interactionKind === "MENSAGEM" ? "Enviar mensagem" : interactionKind === "OBSERVACAO" ? "Registrar observação" : "Criar meta"}
                          <Glyph name="arrow" size={16} />
                        </button>
                      </form>
                    ) : (
                      <p className="muted-copy">O acompanhamento não está ativo. O envio de novas interações fica indisponível.</p>
                    )}
                  </>
                )}
              </section>
            </>
          ) : null}

          {view === "administracao" ? (
            <>
              <PageHeading eyebrow="CONTROLE DO SISTEMA" title="Administração" subtitle="Pendências de integração administrativa." />
              <InfoPanel text="O backend atual contém a entidade Administrador e regras de verificação, mas não publica endpoints REST para listar usuários, aprovar ou rejeitar profissionais, nem moderar contas. Essas operações precisam ser expostas no backend antes de serem conectadas nesta tela." />
              <section className="panel">
                <PanelTitle title="Operações ainda não disponíveis na API" />
                <div className="missing-list">
                  <div><Glyph name="warning" /> Listar usuários e profissionais</div>
                  <div><Glyph name="warning" /> Aprovar ou rejeitar verificação profissional</div>
                  <div><Glyph name="warning" /> Inativar contas e consultar histórico</div>
                  <div><Glyph name="warning" /> Moderação administrativa de publicações</div>
                </div>
              </section>
            </>
          ) : null}

          <footer className="app-footer">
            <span>NutriFollow · Projeto acadêmico</span>
            <span>API REST · Spring Boot · SQL Server</span>
          </footer>
        </div>
      </section>
      {interactionToEdit ? (
        <div className="dialog-backdrop">
          <section className="dialog-panel" role="dialog" aria-modal="true" aria-labelledby="interaction-edit-title">
            <h2 id="interaction-edit-title">Editar {interactionToEdit.tipo === "META" ? "meta" : "observação"}</h2>
            <p>Somente o profissional autor pode alterar esta interação enquanto o acompanhamento estiver ativo.</p>
            <form className="form-stack" onSubmit={saveInteractionEdit}>
              <Field label={interactionToEdit.tipo === "META" ? "Descrição da meta" : "Observação"}>
                <textarea value={interactionEditContent} onChange={(event) => setInteractionEditContent(event.target.value)} maxLength={2000} rows={4} required autoFocus />
              </Field>
              {interactionToEdit.tipo === "META" ? (
                <Field label="Prazo (opcional)"><input type="datetime-local" value={interactionEditDeadline} onChange={(event) => setInteractionEditDeadline(event.target.value)} /></Field>
              ) : null}
              <div className="dialog-actions">
                <button type="button" className="secondary-button" onClick={() => setInteractionToEdit(null)} disabled={busy}>Cancelar</button>
                <button type="submit" className="primary-button" disabled={busy || !interactionEditContent.trim()}>{busy ? "Salvando..." : "Salvar alterações"}</button>
              </div>
            </form>
          </section>
        </div>
      ) : null}
      {confirmation ? (
        <div className="dialog-backdrop">
          <section className="dialog-panel" role="dialog" aria-modal="true" aria-labelledby="confirmation-title">
            <h2 id="confirmation-title">
              {confirmation.kind === "recusar" ? "Recusar solicitação"
                : confirmation.kind === "encerrar" ? "Encerrar acompanhamento"
                  : confirmation.kind === "inativarPublicacao" ? "Inativar publicação"
                    : confirmation.kind === "inativarRegistro" ? "Inativar registro"
                      : "Inativar plano"}
            </h2>
            <p>
              {confirmation.kind === "recusar" ? "A solicitação será marcada como recusada. Informe o motivo para registrar a decisão."
                : confirmation.kind === "encerrar" ? "O vínculo será encerrado e o acesso profissional aos dados deverá cessar conforme as regras do sistema."
                  : confirmation.kind === "inativarPublicacao" ? "A publicação deixará de aparecer no feed. O motivo será armazenado."
                    : confirmation.kind === "inativarRegistro" ? "O registro será inativado, sem apagar o histórico persistido."
                      : "O plano será inativado. Os dados históricos permanecerão no banco."}
            </p>
            <form className="form-stack" onSubmit={submitConfirmation}>
              {confirmation.kind === "recusar" || confirmation.kind === "encerrar" || confirmation.kind === "inativarPublicacao" ? (
                <Field label="Motivo" hint="Até 500 caracteres.">
                  <textarea value={confirmationReason} onChange={(event) => setConfirmationReason(event.target.value)} maxLength={500} rows={3} required autoFocus placeholder="Descreva o motivo..." />
                </Field>
              ) : null}
              <div className="dialog-actions">
                <button type="button" className="secondary-button" onClick={() => { setConfirmation(null); setConfirmationReason(""); }} disabled={busy}>Cancelar</button>
                <button type="submit" className="danger-confirm-button" disabled={busy || ((confirmation.kind === "recusar" || confirmation.kind === "encerrar" || confirmation.kind === "inativarPublicacao") && !confirmationReason.trim())}>
                  {busy ? "Processando..." : confirmation.kind === "recusar" ? "Confirmar recusa" : confirmation.kind === "encerrar" ? "Confirmar encerramento" : "Confirmar inativação"}
                </button>
              </div>
            </form>
          </section>
        </div>
      ) : null}
    </main>
  );
}

function recordDetail(item: Registro) {
  if (item.tipo === "TREINO") {
    return (item.series ?? "—") + " séries · " + (item.repeticoes ?? "—") + " repetições · " + (item.cargaKg ?? "—") + " kg";
  }
  if (item.tipo === "REFEICAO") return item.tipoRefeicao ? statusLabel(item.tipoRefeicao) : "Refeição";
  if (item.tipo === "AGUA") return (item.quantidadeMl ?? "—") + " ml";
  if (item.tipo === "PESO") return (item.pesoKg ?? "—") + " kg";
  return "";
}

function PageHeading({
  eyebrow,
  title,
  subtitle,
}: {
  eyebrow: string;
  title: string;
  subtitle: string;
}) {
  return (
    <div className="page-heading">
      <div><div className="eyebrow dark-eyebrow">{eyebrow}</div><h1>{title}</h1><p>{subtitle}</p></div>
    </div>
  );
}

function PanelTitle({ title, subtitle }: { title: string; subtitle?: string }) {
  return <div className="panel-title"><h2>{title}</h2>{subtitle ? <p>{subtitle}</p> : null}</div>;
}

function EmptyState({ text }: { text: string }) {
  return <div className="empty-state"><span>✳</span><p>{text}</p></div>;
}

function InfoPanel({ text }: { text: string }) {
  return <section className="panel info-panel"><div className="panel-icon"><Glyph name="shield" size={24} /></div><p>{text}</p></section>;
}

function StatusPill({ value }: { value: string }) {
  const kind = value === "ATIVO" || value === "TREINO" ? "good" : value === "PENDENTE" || value === "ACEITO" ? "pending" : value === "RECUSADO" || value === "ENCERRADO" ? "muted" : "neutral";
  return <span className={"status-pill " + kind}>{statusLabel(value)}</span>;
}

function StatCard({
  label,
  value,
  icon,
  tone,
  detail,
}: {
  label: string;
  value: number | string;
  icon: string;
  tone: string;
  detail?: string;
}) {
  return (
    <article className="stat-card">
      <div className={"stat-icon " + tone}><Glyph name={icon} size={19} /></div>
      <div><span>{label}</span><strong>{value}</strong>{detail ? <small>{detail}</small> : null}</div>
    </article>
  );
}

function QuickAction({
  icon,
  title,
  text,
  onClick,
}: {
  icon: string;
  title: string;
  text: string;
  onClick: () => void;
}) {
  return (
    <button className="quick-action" type="button" onClick={onClick}>
      <span className="quick-icon"><Glyph name={icon} size={18} /></span>
      <span><strong>{title}</strong><small>{text}</small></span>
      <Glyph name="arrow" size={16} />
    </button>
  );
}
