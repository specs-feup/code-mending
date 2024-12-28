import logging

class PascalCaseLevelFormatter(logging.Formatter):
    def format(self, record):
        record.levelname = record.levelname.lower().capitalize()
        return super().format(record)

logger = None

def build_logger(name="logger", level=logging.INFO):
    logger = logging.getLogger(name)

    console_handler = logging.StreamHandler()
    file_handler = logging.FileHandler(f"{name}.log")

    console_formatter = PascalCaseLevelFormatter("%(levelname)s: %(message)s")
    file_formatter = logging.Formatter("%(asctime)s - [%(levelname)s] - %(message)s")

    console_handler.setFormatter(console_formatter)
    file_handler.setFormatter(file_formatter)

    logger.addHandler(console_handler)
    logger.addHandler(file_handler)

    logger.setLevel(level)

    return logger

logger = build_logger()
