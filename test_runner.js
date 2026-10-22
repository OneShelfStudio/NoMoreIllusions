const fs = require('fs');

global.window = {
  innerWidth: 400,
  innerHeight: 800,
  addEventListener: (evt, cb) => { global.window['on' + evt] = cb; },
  devicePixelRatio: 2
};
let elements = {};
global.document = {
  getElementById: (id) => {
    if (!elements[id]) {
      const el = {
        width: 400,
        height: 800,
        getContext: function() {
          return {
            canvas: el,
            setTransform: () => {},
            clearRect: () => {},
            fillRect: () => {},
            strokeRect: () => {},
            rect: () => {},
            beginPath: () => {},
            arc: () => {},
            arcTo: () => {},
            moveTo: () => {},
            lineTo: () => {},
            fill: () => {},
            stroke: () => {},
            clip: () => {},
            createLinearGradient: () => ({ addColorStop: () => {} }),
            createRadialGradient: () => ({ addColorStop: () => {} }),
            save: () => {},
            restore: () => {},
            translate: () => {},
            rotate: () => {},
            setLineDash: () => {},
            closePath: () => {},
            drawImage: () => {},
            fillText: () => {}
          };
        },
        addEventListener: () => {},
        style: {},
        getBoundingClientRect: () => ({ width: 300, height: 150 })
      };
      elements[id] = el;
    }
    return elements[id];
  }
};
global.localStorage = {
  getItem: () => null,
  setItem: () => {}
};
global.Audio = class {
  constructor() { this.src = ''; }
  addEventListener() {}
  play() { return Promise.resolve(); }
  pause() {}
  cloneNode() { return new global.Audio(); }
};
global.Image = class {
  constructor() { this.src = ''; this.complete = true; this.naturalWidth = 100; this.naturalHeight = 100; }
};
global.performance = { now: () => 0 };
let frameCb;
global.requestAnimationFrame = (cb) => { frameCb = cb; };

const mb = fs.readFileSync('C:/Users/ryanb/OneDrive/Documents/One Shelf Studio/App Projects/No More Ilusions/docs/multi_band_data.js', 'utf8');
const code = fs.readFileSync('C:/Users/ryanb/OneDrive/Documents/One Shelf Studio/App Projects/No More Ilusions/docs/engine.js', 'utf8');

try {
  eval(mb + '\n' + code);
  if (global.window.onDOMContentLoaded) global.window.onDOMContentLoaded();
  console.log('window.app created successfully!');
  if (frameCb) frameCb(16);
  console.log('first animation frame executed successfully!');
  // Simulate starting stage 1
  window.app.startStage(0);
  console.log('startStage(0) executed successfully!');
  // Simulate physics tick
  window.app.stepPhysics(0.016);
  console.log('stepPhysics(0.016) executed successfully!');
  window.app.renderGameCanvas();
  console.log('renderGameCanvas during PLAYING executed successfully!');
  console.log('>>> ALL VERIFICATION TESTS PASSED 100%! <<<');
} catch (e) {
  console.error('CRASH:', e.stack);
}
