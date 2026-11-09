import express from 'express';
import path from 'path';
import fs from 'fs';
import os from 'os';
import { execFile } from 'child_process';
import { promisify } from 'util';
import { fileURLToPath } from 'url';

const execFileAsync = promisify(execFile);
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 3000;
const HOST = '0.0.0.0';

app.use(express.json({ limit: '10mb' }));

function escapeHtmlServer(str) {
  if (str === undefined || str === null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function buildChromePrintHtml({ records = [], header = {}, orientation = 'portrait' }) {
  const isLandscape = orientation === 'landscape';
  const pageMargin = isLandscape ? '8mm 10mm 8mm 10mm' : '10mm 8mm 10mm 8mm';

  const zila = escapeHtmlServer(header.zila || 'বাগেরহাট');
  const upazila = escapeHtmlServer(header.upazila || 'মোড়েলগঞ্জ');
  const mouza = escapeHtmlServer(header.mouza || '৯৩ পাঠামারা');
  const title = escapeHtmlServer(header.title || 'দলিলে জমির হিসাব');
  const creator = escapeHtmlServer(header.creator || 'মোঃ আব্দুল্লাহ আল রাহাত');
  const dateStr = escapeHtmlServer(header.date || '');

  const tbodyHtml = records.map((record) => {
    const khatians = Array.isArray(record.khatians) && record.khatians.length > 0
      ? record.khatians
      : [{ type: '', no: '-', land: '-', dag: '-' }];
    const kCount = khatians.length;

    const k0 = khatians[0];
    const k0Format = (k0.type && k0.no !== '-')
      ? `<b>${escapeHtmlServer(k0.type)}:</b> ${escapeHtmlServer(k0.no)}`
      : (k0.type ? `<b>${escapeHtmlServer(k0.type)}</b>` : (k0.no !== '-' ? escapeHtmlServer(k0.no) : '-'));
    const k0Dag = (k0.dag && k0.dag !== '-') ? escapeHtmlServer(String(k0.dag).replace(/,\s*/g, ', ')) : '-';

    let rowsHtml = `<tr>
      <td rowspan="${kCount}" class="align-center first-col-cell">${escapeHtmlServer(record.sl_no || '')}</td>
      <td rowspan="${kCount}" class="align-center">${escapeHtmlServer(record.dalil_no || '')}</td>
      <td rowspan="${kCount}" class="align-center">${escapeHtmlServer(record.dateVal || '')}</td>
      <td rowspan="${kCount}">${record.receiversHTML || ''}</td>
      <td rowspan="${kCount}">${record.donorsHTML || ''}</td>
      <td rowspan="${kCount}" class="align-center">${escapeHtmlServer(record.total_land || '')}</td>
      <td class="align-center">${k0Format}</td>
      <td class="align-center">${escapeHtmlServer(k0.land || '-')}</td>
      <td class="align-center dag-cell">${k0Dag}</td>
    </tr>`;

    for (let i = 1; i < kCount; i++) {
      const ki = khatians[i];
      const kiFormat = (ki.type && ki.no !== '-')
        ? `<b>${escapeHtmlServer(ki.type)}:</b> ${escapeHtmlServer(ki.no)}`
        : (ki.type ? `<b>${escapeHtmlServer(ki.type)}</b>` : (ki.no !== '-' ? escapeHtmlServer(ki.no) : '-'));
      const kiDag = (ki.dag && ki.dag !== '-') ? escapeHtmlServer(String(ki.dag).replace(/,\s*/g, ', ')) : '-';
      rowsHtml += `<tr>
        <td class="align-center">${kiFormat}</td>
        <td class="align-center">${escapeHtmlServer(ki.land || '-')}</td>
        <td class="align-center dag-cell">${kiDag}</td>
      </tr>`;
    }

    return `<tbody class="record-group">${rowsHtml}</tbody>`;
  }).join('\n');

  return `<!DOCTYPE html>
<html lang="bn">
<head>
<meta charset="UTF-8">
<title>দলিল ফিগার (Dalil Figure)</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Noto+Serif+Bengali:wght@400;500;600;700&display=swap" rel="stylesheet">
<style>
  @page { size: A4 ${isLandscape ? 'landscape' : 'portrait'}; margin: ${pageMargin}; }
  * {
    box-sizing: border-box !important;
    -webkit-print-color-adjust: exact !important;
    print-color-adjust: exact !important;
  }
  html, body {
    margin: 0 !important;
    padding: 0 !important;
    width: 100% !important;
    background: #ffffff !important;
    color: #000000 !important;
    font-family: 'Noto Serif Bengali', 'SolaimanLipi', 'Kalpurush', serif !important;
  }
  .print-header {
    display: flex !important;
    flex-direction: row !important;
    justify-content: space-between !important;
    align-items: flex-start !important;
    padding: 0 0 6px 0 !important;
    margin: 0 0 8px 0 !important;
    width: 100% !important;
  }
  .header-col-left {
    flex: 1 !important;
    display: flex !important;
    flex-direction: column !important;
    font-size: 11px !important;
    line-height: 1.45 !important;
    color: #000000 !important;
    text-align: left !important;
  }
  .header-col-center {
    flex: 1.3 !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    text-align: center !important;
  }
  .header-col-center h3 {
    font-size: 18px !important;
    color: #000000 !important;
    font-weight: 700 !important;
    text-decoration: underline !important;
    margin: 0 !important;
    line-height: 1.25 !important;
  }
  .header-col-right {
    flex: 1 !important;
    display: flex !important;
    flex-direction: column !important;
    align-items: flex-end !important;
    text-align: right !important;
    font-size: 11px !important;
    line-height: 1.45 !important;
    color: #000000 !important;
  }
  table#main-table {
    font-size: ${isLandscape ? '11.5px' : '10.5px'} !important;
    width: 100% !important;
    border-collapse: separate !important;
    border-spacing: 0 !important;
    border: none !important;
    margin: 0 !important;
    table-layout: fixed !important;
  }
  table#main-table th, table#main-table td {
    border-top: none !important;
    border-left: none !important;
    border-right: 1pt solid #000000 !important;
    border-bottom: 1pt solid #000000 !important;
    padding: ${isLandscape ? '5px 4px' : '4px 3px'} !important;
    color: #000000 !important;
    vertical-align: middle !important;
    line-height: 1.35 !important;
    word-break: break-word !important;
    overflow-wrap: break-word !important;
  }
  table#main-table thead tr:first-child th {
    border-top: 1pt solid #000000 !important;
  }
  table#main-table th:first-child,
  table#main-table td.first-col-cell {
    border-left: 1pt solid #000000 !important;
  }
  table#main-table th {
    background-color: #ededed !important;
    font-weight: 700 !important;
    text-align: center !important;
    font-size: ${isLandscape ? '11px' : '10.5px'} !important;
  }
  .align-center { text-align: center !important; }
  .dag-cell { text-align: center !important; word-break: break-word !important; }
  .person-data {
    padding-bottom: 3px !important;
    margin-bottom: 3px !important;
    border-bottom: 1px dotted #666666 !important;
    line-height: 1.35 !important;
  }
  .person-data:last-child {
    border-bottom: none !important;
    margin-bottom: 0 !important;
    padding-bottom: 0 !important;
  }
  #main-table col.col-1 { width: 4.5% !important; }
  #main-table col.col-2 { width: 8.5% !important; }
  #main-table col.col-3 { width: 9.5% !important; }
  #main-table col.col-4 { width: 21% !important; }
  #main-table col.col-5 { width: 21% !important; }
  #main-table col.col-6 { width: 8% !important; }
  #main-table col.col-7 { width: 9% !important; }
  #main-table col.col-8 { width: 8.5% !important; }
  #main-table col.col-9 { width: 10% !important; }
  tbody.record-group { page-break-inside: avoid !important; break-inside: avoid !important; }
  tr { page-break-inside: avoid !important; break-inside: avoid !important; }
  thead { display: table-header-group !important; }
</style>
</head>
<body>
  <div id="pdf-section">
    <div class="print-header">
      <div class="header-col-left">
        <span><strong>জেলা:</strong> <span>${zila}</span></span>
        <span><strong>উপজেলা:</strong> <span>${upazila}</span></span>
        <span><strong>মৌজা:</strong> <span>${mouza}</span></span>
      </div>
      <div class="header-col-center">
        <h3>${title}</h3>
      </div>
      <div class="header-col-right">
        <span><strong>প্রস্তুতকারক:</strong></span>
        <span>${creator}</span>
        <span style="margin-top:2px;"><strong>তারিখ:</strong> <span>${dateStr}</span></span>
      </div>
    </div>
    <table id="main-table">
      <colgroup>
        <col class="col-1"><col class="col-2"><col class="col-3"><col class="col-4"><col class="col-5">
        <col class="col-6"><col class="col-7"><col class="col-8"><col class="col-9">
      </colgroup>
      <thead>
        <tr>
          <th>নং</th>
          <th>দলিল নং</th>
          <th>তারিখ</th>
          <th>গ্রহীতা(গণ)</th>
          <th>দাতা(গণ)</th>
          <th>মোট জমি</th>
          <th>খতিয়ান</th>
          <th>খতিয়ানের জমি</th>
          <th>দাগ নং</th>
        </tr>
      </thead>
      ${tbodyHtml}
    </table>
  </div>
</body>
</html>`;
}

function findChromeBinary() {
  const candidates = [
    '/ms-playwright/chrome-headless-shell-linux64/chrome-headless-shell',
    '/usr/bin/google-chrome',
    '/usr/bin/chromium',
    '/usr/bin/chromium-browser'
  ];
  for (const bin of candidates) {
    if (fs.existsSync(bin)) return bin;
  }
  return null;
}

app.post('/api/generate-pdf', async (req, res) => {
  const chromeBin = findChromeBinary();
  if (!chromeBin) {
    return res.status(503).json({ error: 'Chrome binary not available on server' });
  }

  const { records, header, orientation } = req.body || {};
  if (!Array.isArray(records) || records.length === 0) {
    return res.status(400).json({ error: 'No records provided' });
  }

  const id = `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
  const tmpHtmlPath = path.join(os.tmpdir(), `dalil_print_${id}.html`);
  const tmpPdfPath = path.join(os.tmpdir(), `dalil_print_${id}.pdf`);

  try {
    const html = buildChromePrintHtml({ records, header, orientation });
    await fs.promises.writeFile(tmpHtmlPath, html, 'utf8');

    await execFileAsync(chromeBin, [
      '--headless',
      '--no-sandbox',
      '--disable-gpu',
      '--disable-dev-shm-usage',
      '--no-pdf-header-footer',
      `--print-to-pdf=${tmpPdfPath}`,
      `file://${tmpHtmlPath}`
    ], { timeout: 25000 });

    const pdfBuffer = await fs.promises.readFile(tmpPdfPath);
    res.setHeader('Content-Type', 'application/pdf');
    res.setHeader('Content-Disposition', 'attachment; filename="Dalil_Figure.pdf"');
    res.send(pdfBuffer);
  } catch (err) {
    console.error('Error generating Chrome PDF:', err);
    res.status(500).json({ error: 'Failed to generate PDF', details: err.message });
  } finally {
    fs.promises.unlink(tmpHtmlPath).catch(() => {});
    fs.promises.unlink(tmpPdfPath).catch(() => {});
  }
});

// Serve static assets with correct headers
app.use(express.static(__dirname, {
  setHeaders: (res, filePath) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    if (filePath.endsWith('service-worker.js')) {
      res.setHeader('Service-Worker-Allowed', '/');
      res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
    } else if (filePath.endsWith('manifest.json')) {
      res.setHeader('Content-Type', 'application/manifest+json; charset=utf-8');
      res.setHeader('Cache-Control', 'no-cache');
    } else if (filePath.endsWith('.png')) {
      res.setHeader('Content-Type', 'image/png');
    }
  }
}));

// Route fallback for client-side navigation
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.listen(PORT, HOST, () => {
  console.log(`Dalil Record server running on http://${HOST}:${PORT}`);
});
