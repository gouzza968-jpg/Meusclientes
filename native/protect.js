// Ofusca o JavaScript do app (roda só no build do APK; o código-fonte do repositório continua legível para você)
const fs = require('fs');
const JO = require('javascript-obfuscator');
const file = process.argv[2];
let html = fs.readFileSync(file, 'utf8');
const a = html.indexOf('<script>');
const b = html.indexOf('</script>', a);
if (a < 0 || b < 0) { console.error('script não encontrado'); process.exit(1); }
const code = html.slice(a + 8, b);
const out = JO.obfuscate(code, {
  compact: true, simplify: true, target: 'browser',
  stringArray: true, stringArrayThreshold: 1, stringArrayEncoding: ['base64'],
  stringArrayRotate: true, stringArrayShuffle: true, stringArrayWrappersCount: 2,
  identifierNamesGenerator: 'hexadecimal', renameGlobals: false,
  transformObjectKeys: false, controlFlowFlattening: false, deadCodeInjection: false,
  selfDefending: false, debugProtection: false, splitStrings: false
}).getObfuscatedCode();
const header = '/* © Telão Play - Meus clientes. Código protegido: cópia, engenharia reversa e modificação não autorizadas são proibidas. */\n';
html = html.slice(0, a + 8) + header + out + html.slice(b);
fs.writeFileSync(file, html);
fs.writeFileSync('/tmp/_check.js', out);
console.log('ofuscado:', code.length, '->', out.length);
