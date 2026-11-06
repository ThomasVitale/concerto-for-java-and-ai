/// <reference types="vite/client" />
import { html, css, LitElement, PropertyValues } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import { Sequencer, WorkletSynthesizer } from 'spessasynth_lib';
// Vite emits the worklet as an asset of this app, so it is always served from our own origin.
import workletUrl from 'spessasynth_lib/dist/spessasynth_processor.min.js?url';

type SharedAudio = { context: AudioContext; synth: WorkletSynthesizer; ready: Promise<void> };

let shared: SharedAudio | null = null;

/** One AudioContext and one soundfont per page, created on the first user gesture. */
async function audio(soundfontUrl: string): Promise<SharedAudio> {
  if (shared) {
    await shared.ready;
    return shared;
  }
  const context = new AudioContext();
  await context.audioWorklet.addModule(workletUrl);
  const synth = new WorkletSynthesizer(context);
  synth.connect(context.destination);
  const ready = (async () => {
    const response = await fetch(soundfontUrl);
    if (!response.ok) {
      throw new Error(`Soundfont not available (${response.status})`);
    }
    await synth.soundBankManager.addSoundBank(await response.arrayBuffer(), 'main');
    await synth.isReady;
  })();
  shared = { context, synth, ready };
  await ready;
  return shared;
}

/**
 * Plays a MIDI progression in the browser with a General MIDI soundfont and shows which chord is sounding.
 */
@customElement('mousike-chord-progression-player')
export class ChordProgressionPlayer extends LitElement {
  @property({ type: String }) src = '';
  @property({ type: String }) soundfont = '/soundfonts/GeneralUserGS.sf3';
  @property({ type: Array }) chords: string[] = [];
  @property({ type: Number }) secondsPerChord = 2;
  @property({ type: Number }) seconds = 0;

  @state() private status: 'idle' | 'loading' | 'playing' | 'paused' | 'error' = 'idle';
  @state() private time = 0;
  @state() private error = '';

  private sequencer: Sequencer | null = null;
  private frame = 0;

  static styles = css`
    :host {
      display: block;
      --player-lamp: var(--studio-red, #cf2031);
      --player-ink: var(--studio-ink, #1b1d20);
      --player-muted: var(--studio-ink-soft, #5f656b);
      --player-line: var(--studio-rule, #cdd1d4);
      --player-pad: var(--studio-keycap, #eef1f4);
      --player-pad-edge: var(--studio-keycap-edge, #d9dde2);
      font: inherit;
      color: var(--player-ink);
    }
    .player {
      display: grid;
      grid-template-columns: auto minmax(0, 1fr);
      gap: 0.75rem 1rem;
      align-items: center;
    }
    /* The transport button: black key, amber lamp when running */
    button {
      width: 2.75rem;
      height: 2.75rem;
      border-radius: 50%;
      border: none;
      background: var(--player-ink);
      color: #f6f6f3;
      cursor: pointer;
      display: grid;
      place-items: center;
      font: inherit;
      box-shadow: 0 1px 2px rgba(16, 24, 40, 0.2);
    }
    button.running {
      background: var(--player-lamp);
      box-shadow: 0 0 0 3px color-mix(in srgb, var(--player-lamp) 30%, transparent);
    }
    button:focus-visible {
      outline: 2px solid var(--player-ink);
      outline-offset: 2px;
    }
    button[disabled] {
      opacity: 0.5;
      cursor: progress;
    }
    svg {
      width: 1.1rem;
      height: 1.1rem;
      fill: currentColor;
    }
    /* Pads: one per chord, lit while it sounds */
    .chords {
      display: flex;
      flex-wrap: wrap;
      gap: 0.4rem;
    }
    .chord {
      min-width: 3rem;
      padding: 0.45rem 0.6rem;
      border-radius: 7px;
      border: 1px solid var(--player-pad-edge);
      background: var(--player-pad);
      box-shadow: inset 0 -2px 0 rgba(0, 0, 0, 0.05);
      font-family: "SF Mono", ui-monospace, Menlo, monospace;
      font-size: 0.85rem;
      text-align: center;
      transition: background 90ms ease, color 90ms ease, box-shadow 90ms ease;
    }
    .chord.now {
      background: var(--player-lamp);
      border-color: var(--player-lamp);
      color: white;
      box-shadow: 0 0 10px color-mix(in srgb, var(--player-lamp) 55%, transparent);
    }
    .track {
      grid-column: 1 / -1;
      height: 4px;
      border-radius: 2px;
      background: var(--player-line);
      overflow: hidden;
    }
    .track > div {
      height: 100%;
      background: var(--player-lamp);
      transition: width 100ms linear;
    }
    .status {
      grid-column: 1 / -1;
      color: var(--player-muted);
      font-size: 0.8rem;
    }
    .status.error {
      color: var(--studio-led-red, #d9372c);
    }
    @media (prefers-reduced-motion: reduce) {
      .chord, .track > div { transition: none; }
    }
  `;

  disconnectedCallback(): void {
    super.disconnectedCallback();
    this.pause();
  }

  protected updated(changed: PropertyValues): void {
    if (changed.has('src') && this.sequencer) {
      this.pause();
      this.sequencer = null;
      this.time = 0;
    }
  }

  render() {
    const current = this.secondsPerChord > 0 ? Math.floor(this.time / this.secondsPerChord) : -1;
    const total = this.seconds || this.chords.length * this.secondsPerChord;
    const progress = total > 0 ? Math.min(100, (this.time / total) * 100) : 0;
    const playing = this.status === 'playing';
    return html`
      <div class="player">
        <button
          class=${playing ? 'running' : ''}
          @click=${() => (playing ? this.pause() : this.play())}
          ?disabled=${this.status === 'loading'}
          aria-label=${playing ? 'Pause chord progression' : 'Play chord progression'}
        >
          ${playing
            ? html`<svg viewBox="0 0 16 16"><rect x="3" y="2" width="4" height="12" rx="1"/><rect x="9" y="2" width="4" height="12" rx="1"/></svg>`
            : html`<svg viewBox="0 0 16 16"><path d="M4 2.5v11l9-5.5z"/></svg>`}
        </button>
        <div class="chords" aria-hidden="true">
          ${this.chords.map((chord, index) => html`<span class="chord ${playing && index === current ? 'now' : ''}">${chord}</span>`)}
        </div>
        <div class="track" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow=${Math.round(progress)}>
          <div style="width:${progress}%"></div>
        </div>
        ${this.status === 'loading' ? html`<div class="status">Loading the piano…</div>` : ''}
        ${this.status === 'error' ? html`<div class="status error">${this.error}</div>` : ''}
      </div>
    `;
  }

  private async play(): Promise<void> {
    try {
      this.status = 'loading';
      const { context, synth } = await audio(this.soundfont);
      await context.resume();
      if (!this.sequencer) {
        const response = await fetch(this.src);
        if (!response.ok) {
          throw new Error(`Chord progression not available (${response.status})`);
        }
        const midi = await response.arrayBuffer();
        this.sequencer = new Sequencer(synth);
        this.sequencer.loopCount = 0;
        this.sequencer.loadNewSongList([{ binary: midi }]);
      }
      if (this.sequencer.isFinished || this.time >= (this.sequencer.duration || Infinity)) {
        this.sequencer.currentTime = 0;
      }
      this.sequencer.play();
      this.status = 'playing';
      this.tick();
    } catch (e) {
      this.status = 'error';
      this.error = e instanceof Error ? e.message : String(e);
    }
  }

  private pause(): void {
    cancelAnimationFrame(this.frame);
    if (this.sequencer && this.status === 'playing') {
      this.sequencer.pause();
    }
    if (this.status === 'playing') {
      this.status = 'paused';
    }
  }

  private tick = (): void => {
    if (!this.sequencer || this.status !== 'playing') {
      return;
    }
    this.time = this.sequencer.currentTime;
    const total = this.seconds || this.sequencer.duration;
    if (this.sequencer.isFinished || this.sequencer.paused || (total > 0 && this.time >= total)) {
      this.sequencer.pause();
      this.status = 'idle';
      this.time = 0;
      return;
    }
    this.frame = requestAnimationFrame(this.tick);
  };
}
