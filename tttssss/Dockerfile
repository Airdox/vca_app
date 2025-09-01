# Use a smaller base image for the final stage
FROM python:3.10-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    gcc \
    build-essential \
    && pip install --no-cache-dir TTS \
    && apt-get purge -y --auto-remove gcc build-essential \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

CMD ["tts", "--model_name", "tts_models/de/thorsten/tacotron2-DDC", "--text", "Hallo, das ist ein Test."]