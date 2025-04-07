/**
 * YouTube Video Summarizer Application
 */

const YT = {
    regex: /^(https?:\/\/)?(www\.)?(youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]{11})($|&.+)/,
    idIdx: 4
};

const API = {
    url: '/ytsummarizer/api/summary',
    timeout: 60000
};

const ERRORS = {
    timeout: 'Request timed out. Video too long or server busy.',
    invalid: 'Invalid YouTube URL',
    empty: 'Enter a YouTube URL',
    server: 'Server error processing video',
    unknown: 'Unknown error occurred'
};

function checkUrl(url) {
    const trim = url?.trim() || '';
    const valid = trim && YT.regex.test(trim);

    return {
        trim,
        valid,
        id: valid ? trim.match(YT.regex)[YT.idIdx] : null
    };
}

async function getSummary(url, callbacks) {
    const { start, success, error, done } = callbacks;
    const { valid, trim } = checkUrl(url);

    if (!valid) {
        error(trim ? ERRORS.invalid : ERRORS.empty);
        return;
    }

    start();

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), API.timeout);

    try {
        const resp = await fetch(`${API.url}?url=${encodeURIComponent(trim)}`, {
            signal: controller.signal
        });

        if (!resp.ok) {
            throw new Error(resp.status === 500 ? ERRORS.server : `Status: ${resp.status}`);
        }

        const { summary = 'No summary available' } = await resp.json();
        success(summary);
    } catch (err) {
        error(`Error: ${err.name === 'AbortError' ? ERRORS.timeout : (err.message || ERRORS.unknown)}`);
    } finally {
        clearTimeout(timeout);
        done();
    }
}

function UI({ state, handlers }) {
    const { url, error, loading, summary } = state;
    const { onChange, onSubmit } = handlers;

    return (
        <div className="container">
            <h1>YouTube Video Summarizer</h1>

            <form className="form" onSubmit={onSubmit}>
                <input
                    type="text"
                    className="input"
                    placeholder="Enter YouTube URL"
                    value={url}
                    onChange={onChange}
                    disabled={loading}
                />
                <button type="submit" className="button" disabled={loading || !url.trim()}>
                    {loading ? 'Processing...' : 'Summarize'}
                </button>
            </form>

            {error && <div className="error" role="alert">{error}</div>}

            {loading && (
                <div className="loading">
                    <p>Analyzing video, please wait...</p>
                    <p className="details">May take several minutes for longer videos</p>
                </div>
            )}

            {summary && (
                <div className="summary">
                    <h2>Video Summary</h2>
                    <div className="content">{summary}</div>
                </div>
            )}
        </div>
    );
}

function App() {
    const [state, setState] = React.useState({
        url: '',
        error: '',
        loading: false,
        summary: ''
    });

    const handlers = {
        onChange: e => setState({ ...state, url: e.target.value, error: '' }),
        onSubmit: e => {
            e?.preventDefault();

            getSummary(state.url, {
                start: () => setState({ ...state, error: '', summary: '', loading: true }),
                success: summary => setState({ ...state, summary, loading: false }),
                error: error => setState({ ...state, error, loading: false }),
                done: () => {}
            });
        }
    };

    return <UI state={state} handlers={handlers} />;
}

try {
    const root = document.getElementById('root');
    ReactDOM.createRoot(root).render(<App />);
} catch (err) {
    document.getElementById('root').innerHTML = `
        <div class="error">
            <h2>Failed to start app</h2>
            <p>Please refresh the page</p>
        </div>
    `;
}