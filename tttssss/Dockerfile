# Use a smaller base image for the final stage
FROM python:3.10-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    gcc \
    build-essential \
    espeak-ng \
    && pip install --no-cache-dir \
    "torch>=2.0.1" "torchaudio>=2.0.2" numpy scipy soundfile pyyaml tqdm packaging psutil requests einops "scikit-learn" pandas num2words inflect unidecode eng_to_ipa pyopenjtalk-prebuilt jieba ko_pron bangla g2p_en pypinyin jamo cn2an anyascii openai-whisper language-tags beautifulsoup4 lxml "huggingface-hub" fsspec s3fs loguru \
    && pip install --no-cache-dir --no-deps TTS \
    && pip install --no-cache-dir "gruut[de]" \
    && apt-get purge -y --auto-remove gcc build-essential \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /var/cache/apt/*

WORKDIR /app

CMD ["tts", "--model_name", "tts_models/de/thorsten/tacotron2-DDC", "--text", "Hallo, das ist ein Test."]
