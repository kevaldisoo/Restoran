<!-- Admin: laudade positsioonide muutmine. Esialgne disain Claude Codega, pärast veidi muudetud -->
<template>
  <div class="admin-page">
    <div class="admin-header">
      <h1 class="admin-title">Admin - Laudade positsioonid</h1>
      <div class="admin-actions">
        <span v-if="hasOverlap" class="overlap-warning">⚠️ Lauad kattuvad</span>
        <button class="btn-save" :disabled="hasOverlap || !dirty" @click="save">
          {{ saving ? 'Salvestab…' : 'Salvesta' }}
        </button>
        <button class="btn-reset" @click="reset">Taasta</button>
        <router-link class="btn-back" to="/">← Tagasi</router-link>
      </div>
    </div>

    <div v-if="loadError" class="load-error">{{ loadError }}</div>

    <div class="svg-wrapper">
      <svg
        ref="svgEl"
        class="admin-svg"
        viewBox="0 0 900 685"
        xmlns="http://www.w3.org/2000/svg"
        @mousemove.prevent="onMouseMove"
        @mouseup="onMouseUp"
        @mouseleave="onMouseUp"
      >
        <!-- Tsoonide taustad -->
        <rect x="10" y="10" width="545" height="420" rx="6" class="zone inside-zone" />
        <text x="282" y="32" class="zone-label">Sisesaal</text>
        <rect x="570" y="10" width="320" height="420" rx="6" class="zone terrace-zone" />
        <text x="730" y="32" class="zone-label">Terrass</text>
        <rect x="10" y="445" width="880" height="230" rx="6" class="zone private-zone" />
        <text x="450" y="467" class="zone-label">Privaatruum</text>

        <!-- Akna all piirjooned -->
        <line x1="10" y1="150" x2="555" y2="150" class="guide-line" />
        <text x="12" y="147" class="guide-label">y&lt;150 → akna all</text>
        <line x1="400" y1="10" x2="400" y2="430" class="guide-line" />
        <text x="403" y="22" class="guide-label">x&gt;400 → akna all</text>

        <!-- Lastenurga punkt ja raadius -->
        <circle cx="10" cy="350" r="200" class="lastenurk-radius" />
        <circle cx="10" cy="350" r="6" class="lastenurk-dot" />
        <text x="22" y="346" class="guide-label lastenurk-label">Lastenurk</text>

        <!-- Lauad -->
        <g
          v-for="laud in lauad"
          :key="laud.id"
          class="table-group"
          @mousedown.prevent="startDrag(laud, $event)"
        >
          <rect
            :x="laud.posX"
            :y="laud.posY"
            :width="laud.width"
            :height="laud.height"
            rx="4"
            :fill="overlapIds.has(laud.id) ? '#fca5a5' : '#93c5fd'"
            :stroke="dragging?.id === laud.id ? '#1d4ed8' : '#475569'"
            :stroke-width="dragging?.id === laud.id ? 2.5 : 1.5"
            class="table-rect"
          />
          <text
            :x="laud.posX + laud.width / 2"
            :y="laud.posY + laud.height / 2 - 5"
            class="tbl-num"
          >
            {{ laud.lauaNumber }}
          </text>
          <text
            :x="laud.posX + laud.width / 2"
            :y="laud.posY + laud.height / 2 + 9"
            class="tbl-cap"
          >
            {{ laud.mahutavus }}p
          </text>
          <text
            v-if="laud.aknaAll"
            :x="laud.posX + 2"
            :y="laud.posY + 12"
            class="icon-text"
            title="Akna all"
          >
            🪟
          </text>
          <text
            v-if="laud.lastenurk"
            :x="laud.posX + laud.width - 10"
            :y="laud.posY + 12"
            class="icon-text"
            title="Lastenurga lähedal"
          >
            🕹️
          </text>
        </g>
      </svg>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '@/api/index.js'

const svgEl = ref(null)
const lauad = ref([])
const originals = ref([])
const dragging = ref(null)
const dragOffset = ref({ x: 0, y: 0 })
const dirty = ref(false)
const saving = ref(false)
const loadError = ref('')

onMounted(async () => {
  try {
    const data = await api.getLauad()
    lauad.value = data.map((l) => ({ ...l }))
    originals.value = data.map((l) => ({ ...l }))
  } catch (e) {
    loadError.value = 'Laudade laadimine ebaõnnestus: ' + e.message
  }
})

// ── Funktsioonid, mis arvutavad lastenurga lähedust või akna all olemist ────────────────────────────────────────────────────────────

function computeAknaAll(l) {
  if (l.tsoon === 'PRIVAATRUUM') return false // Privaatruumis pole aknaid
  return l.posY < 150 || l.posX > 400 || l.tsoon === 'TERRASS'
}

function computeLastenurk(l) {
  const cx = l.posX + l.width / 2
  const cy = l.posY + l.height / 2
  return Math.sqrt((cx - 10) ** 2 + (cy - 350) ** 2) <= 200
}

// ── Kontroll, ega lauad ei kattu üksteisega ────────────────────────────────────────────────────────

function overlaps(a, b) {
  return (
    a.posX < b.posX + b.width &&
    a.posX + a.width > b.posX &&
    a.posY < b.posY + b.height &&
    a.posY + a.height > b.posY
  )
}

const overlapIds = computed(() => {
  const ids = new Set()
  for (let i = 0; i < lauad.value.length; i++) {
    for (let j = i + 1; j < lauad.value.length; j++) {
      if (overlaps(lauad.value[i], lauad.value[j])) {
        ids.add(lauad.value[i].id)
        ids.add(lauad.value[j].id)
      }
    }
  }
  return ids
})

const hasOverlap = computed(() => overlapIds.value.size > 0)

// ── Liigutamine kasutades SVGGraphicsElement ─────────────────────────────────────────────────────────────────────

function svgPoint(event) {
  const pt = svgEl.value.createSVGPoint()
  pt.x = event.clientX
  pt.y = event.clientY
  return pt.matrixTransform(svgEl.value.getScreenCTM().inverse())
}

function startDrag(laud, event) {
  const pt = svgPoint(event)
  dragging.value = laud
  dragOffset.value = { x: pt.x - laud.posX, y: pt.y - laud.posY }
}

function onMouseMove(event) {
  if (!dragging.value) return
  const pt = svgPoint(event)
  const laud = lauad.value.find((l) => l.id === dragging.value.id)
  if (!laud) return
  laud.posX = Math.round(pt.x - dragOffset.value.x)
  laud.posY = Math.round(pt.y - dragOffset.value.y)
  laud.aknaAll = computeAknaAll(laud)
  laud.lastenurk = computeLastenurk(laud)
  dirty.value = true
}

function onMouseUp() {
  dragging.value = null
}

// ── Save / Reset ─────────────────────────────────────────────────────────────

async function save() {
  saving.value = true
  try {
    const changed = lauad.value.filter((l) => {
      const orig = originals.value.find((o) => o.id === l.id)
      return orig && (orig.posX !== l.posX || orig.posY !== l.posY)
    })
    for (const l of changed) {
      await api.updateLauaPositsioon(l.id, { posX: l.posX, posY: l.posY })
    }
    originals.value = lauad.value.map((l) => ({ ...l }))
    dirty.value = false
  } finally {
    saving.value = false
  }
}

function reset() {
  lauad.value = originals.value.map((l) => ({ ...l }))
  dirty.value = false
}
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: #f9f5f0;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.admin-header {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.admin-title {
  font-size: 1.3rem;
  font-weight: 800;
  color: #78350f;
  margin: 0;
  flex: 1;
}

.admin-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.overlap-warning {
  color: #dc2626;
  font-size: 0.85rem;
  font-weight: 600;
}

.btn-save,
.btn-reset,
.btn-back {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  border: none;
}

.btn-save {
  background: #d97706;
  color: #fff;
  transition: background 0.15s;
}
.btn-save:hover:not(:disabled) {
  background: #b45309;
}
.btn-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-reset {
  background: transparent;
  border: 1px solid #d1d5db;
  color: #374151;
}
.btn-reset:hover {
  background: #f3f4f6;
}

.btn-back {
  background: transparent;
  border: 1px solid #d1d5db;
  color: #374151;
  display: inline-flex;
  align-items: center;
}
.btn-back:hover {
  background: #f3f4f6;
}

.load-error {
  color: #dc2626;
  font-size: 0.9rem;
}

.svg-wrapper {
  background: #f8f4ef;
  border: 1px solid #e2d8cc;
  border-radius: 8px;
  overflow: hidden;
  padding: 8px;
}

.admin-svg {
  width: 100%;
  height: auto;
  display: block;
  user-select: none;
}

.zone {
  fill-opacity: 0.35;
  stroke-width: 1.5;
}
.inside-zone {
  fill: #fde68a;
  stroke: #d97706;
}
.terrace-zone {
  fill: #a7f3d0;
  stroke: #059669;
}
.private-zone {
  fill: #ddd6fe;
  stroke: #7c3aed;
}

.zone-label {
  font-size: 13px;
  font-weight: 700;
  fill: #374151;
  text-anchor: middle;
  dominant-baseline: middle;
}

.guide-line {
  stroke: #0ea5e9;
  stroke-dasharray: 6 4;
  stroke-width: 1;
  opacity: 0.7;
}

.guide-label {
  font-size: 8px;
  fill: #0369a1;
}

.lastenurk-radius {
  fill: #fed7aa;
  fill-opacity: 0.25;
  stroke: #f97316;
  stroke-dasharray: 6 4;
  stroke-width: 1.5;
}

.lastenurk-dot {
  fill: #f97316;
}

.lastenurk-label {
  fill: #c2410c;
}

.table-group {
  cursor: grab;
}
.table-group:active {
  cursor: grabbing;
}

.table-rect {
  transition: filter 0.1s;
}
.table-group:hover .table-rect {
  filter: brightness(0.92);
}

.tbl-num {
  font-size: 10px;
  font-weight: 700;
  fill: #1e3a5f;
  text-anchor: middle;
  dominant-baseline: middle;
  pointer-events: none;
}

.tbl-cap {
  font-size: 9px;
  fill: #334155;
  text-anchor: middle;
  dominant-baseline: middle;
  pointer-events: none;
}

.icon-text {
  font-size: 8px;
  pointer-events: none;
}
</style>
