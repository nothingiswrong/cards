<script setup>
import { ref } from 'vue'

const loading = ref(false)
const error = ref('')
const data = ref(null)

async function runSimulation() {
  loading.value = true
  error.value = ''
  data.value = null
  try {
    const res = await fetch('/api/simulation/run', { method: 'POST' })
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}`)
    }
    data.value = await res.json()
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="app">
    <header class="header">
      <h1>Симуляция оплаты картами</h1>
      <p class="lead">
        Запускает серверную симуляцию: кошелёк после прогона и покупки (шаги — текстом в каждой записи).
      </p>
      <button
        type="button"
        class="run-btn"
        :disabled="loading"
        @click="runSimulation"
      >
        {{ loading ? 'Выполняется…' : 'Запустить симуляцию' }}
      </button>
      <p v-if="error" class="error" role="alert">{{ error }}</p>
    </header>

    <main v-if="data" class="main">
      <section
        v-if="data.cards?.length"
        class="wallet-section"
        aria-labelledby="wallet-heading"
      >
        <h2 id="wallet-heading">
          Кошелёк после симуляции
          <span class="count">({{ data.cards.length }})</span>
        </h2>
        <ul class="wallet-list">
          <li
            v-for="(c, i) in data.cards"
            :key="`${c.cardNumber}-${i}`"
            class="wallet-item"
          >
            <div class="wallet-row">
              <span class="wallet-type">{{ c.cardType }}</span>
              <span class="wallet-name">«{{ c.name }}»</span>
              <span class="wallet-num mono">№ {{ c.cardNumber }}</span>
            </div>
            <p class="wallet-details">{{ c.details }}</p>
          </li>
        </ul>
      </section>

      <section
        v-if="data.purchases?.length"
        class="purchases-section"
        aria-labelledby="purchases-heading"
      >
        <h2 id="purchases-heading">
          Покупки
          <span class="count">({{ data.purchases.length }})</span>
        </h2>
        <article
          v-for="(p, i) in data.purchases"
          :key="`${p.purchaseId}-${i}`"
          class="purchase-card"
        >
          <div class="purchase-head">
            <span class="badge">#{{ p.purchaseId }}</span>
            <span class="meta">
              Сумма: <strong>{{ p.initialAmount }}</strong>
              · Остаток: <strong>{{ p.remainingAmount }}</strong>
            </span>
          </div>
          <div class="status-row">
            <span class="pill">{{ p.purchaseStatus }}</span>
            <span class="pill pill--kind">{{ p.simulationResultKind }}</span>
          </div>
          <div class="steps-block">
            <div class="steps-label">Шаги (текст)</div>
            <pre class="steps-pre">{{ p.stepsText || '(нет шагов)' }}</pre>
          </div>
        </article>
      </section>

      <p
        v-if="!data.cards?.length && !data.purchases?.length"
        class="empty"
      >
        Нет данных в ответе.
      </p>
    </main>
  </div>
</template>

<style scoped>
.app {
  max-width: 880px;
  margin: 0 auto;
  padding: 2rem 1.25rem 4rem;
  font-family: system-ui, 'Segoe UI', Roboto, sans-serif;
  color: #1a1a1e;
}

.header {
  margin-bottom: 2rem;
}

h1 {
  font-size: 1.75rem;
  font-weight: 600;
  margin: 0 0 0.5rem;
  letter-spacing: -0.02em;
}

h2 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 1rem;
}

.count {
  font-weight: 500;
  color: #71717a;
  font-size: 0.95em;
}

.lead {
  margin: 0 0 1.25rem;
  color: #5c5c66;
  font-size: 0.95rem;
  line-height: 1.5;
}

.run-btn {
  appearance: none;
  border: none;
  border-radius: 10px;
  padding: 0.65rem 1.35rem;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(165deg, #6d5efc, #4f46e5);
  color: #fff;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(79, 70, 229, 0.35);
}

.run-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  box-shadow: none;
}

.error {
  margin-top: 1rem;
  color: #b91c1c;
  font-size: 0.9rem;
}

.main {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.wallet-section {
  border: 1px solid #e8e8ef;
  border-radius: 14px;
  padding: 1.25rem 1.25rem 0.5rem;
  background: #f8f9ff;
}

.wallet-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.wallet-item {
  border-top: 1px solid #e4e4ef;
  padding: 0.9rem 0;
}

.wallet-item:first-of-type {
  border-top: none;
  padding-top: 0.25rem;
}

.wallet-row {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 0.35rem 0.75rem;
  margin-bottom: 0.35rem;
}

.wallet-type {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #4f46e5;
}

.wallet-name {
  font-weight: 600;
  color: #1a1a1e;
}

.wallet-num {
  font-size: 0.82rem;
  color: #52525b;
}

.mono {
  font-family: ui-monospace, Consolas, monospace;
}

.wallet-details {
  margin: 0;
  font-size: 0.85rem;
  line-height: 1.45;
  color: #3f3f46;
}

.purchase-card {
  border: 1px solid #e8e8ef;
  border-radius: 14px;
  padding: 1.1rem 1.25rem;
  margin-bottom: 1rem;
  background: #fafafa;
}

.purchase-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem 1rem;
  margin-bottom: 0.75rem;
}

.badge {
  font-family: ui-monospace, Consolas, monospace;
  font-size: 0.85rem;
  background: #e0e7ff;
  color: #3730a3;
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
}

.meta {
  font-size: 0.9rem;
  color: #444;
}

.status-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.pill {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 0.25rem 0.55rem;
  border-radius: 999px;
  background: #ecfdf5;
  color: #047857;
  font-weight: 600;
}

.pill--kind {
  background: #fff7ed;
  color: #c2410c;
}

.steps-label {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #71717a;
  margin-bottom: 0.35rem;
}

.steps-pre {
  margin: 0;
  padding: 0.85rem 1rem;
  background: #fff;
  border: 1px solid #e4e4e9;
  border-radius: 10px;
  font-size: 0.8rem;
  line-height: 1.45;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, Consolas, monospace;
}

.empty {
  color: #71717a;
  font-size: 0.95rem;
  margin: 0;
}

@media (prefers-color-scheme: dark) {
  .app {
    color: #f4f4f5;
  }

  .lead {
    color: #a1a1aa;
  }

  .wallet-section {
    background: #1a1b22;
    border-color: #2e2e36;
  }

  .wallet-item {
    border-top-color: #2e2e36;
  }

  .wallet-name {
    color: #f4f4f5;
  }

  .wallet-details {
    color: #d4d4d8;
  }

  .purchase-card {
    background: #1c1c21;
    border-color: #2e2e36;
  }

  .steps-pre {
    background: #16161a;
    border-color: #2e2e36;
  }

  .meta {
    color: #d4d4d8;
  }

  .count {
    color: #a1a1aa;
  }
}
</style>
