import logging

from apscheduler.schedulers.background import BackgroundScheduler

from .jobs import jobs

logger = logging.getLogger('job_scheduler')
scheduler = BackgroundScheduler(daemon=True)


def schedule_jobs(mqtt_client):
    for job in jobs:
        # Schedule job with library
        logger.info(
            f'Scheduling job {job.name} at {job.hour}:{job.minute}:{job.second}'
        )
        scheduler.add_job(
            lambda: job.run(mqtt_client),
            'cron',
            hour=job.hour,
            minute=job.minute,
            second=job.second,
            jitter=120,
            name=job.name,
            max_instances=1,
            id=job.name,
            replace_existing=True,
        )

    if not scheduler.running:
        scheduler.start()

def unschedule_jobs():
    for job in jobs:
        scheduler.remove_job(job.name)
