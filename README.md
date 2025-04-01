# YouTube Video Summarizer

A simple audio-parsing command-line tool 

## Prerequisites

- java 17+
- maven
- yt-dlp
- whisper 
- claude API key (Anthropic)

 Build the project: `mvn clean package`

## Usage

**Build**
```bash
mvn clean package
```
**Run**
```bash
CLAUDE_API_KEY=your-api-key java -jar target/video-summary.jar https://www.youtube.com/watch?v=example
```

## Process  
1. Download audio
2. Transcribe with Whisper
3. Get Summary from Claude 
4. Output To Console

**Note:** will use db storage for already requested links


