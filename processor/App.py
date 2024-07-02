import os

from flask import Flask, request, jsonify, send_file
import redis
import datetime
import threading
import time
import pandas as pd
from statsmodels.tsa.arima.model import ARIMA
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy import func
import json
import matplotlib.pyplot as plt
import seaborn as sns
import io
import matplotlib
from dotenv import load_dotenv

load_dotenv()

database_url = os.getenv('DATABASE_URL')
port_no = os.getenv('PORT')

matplotlib.use('Agg')

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = database_url
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False  # Disable tracking modifications

db = SQLAlchemy(app)

forecast_steps = 12

redis_client = redis.StrictRedis(host='localhost', port=6379)


class PredictiveAnalysisResult(db.Model):
    __tablename__ = 'predictive_analysis_result'

    sl_no = db.Column(db.Integer, primary_key=True)
    request_key = db.Column(db.String(255), nullable=False)
    result_set = db.Column(JSONB)
    series_data = db.Column(JSONB)
    no_of_time_accessed = db.Column(db.Integer, nullable=False, default=0)
    last_accessed_on = db.Column(db.TIMESTAMP, server_default=func.now())
    last_accessed_from = db.Column(db.String(20))
    is_block = db.Column(db.Integer, default=0)

    def __repr__(self):
        return f'<PredictiveAnalysisResult sl_no={self.sl_no} request_key={self.request_key}>'


with app.app_context():
    db.create_all()


def fetch_result_from_db(request_key):
    result = PredictiveAnalysisResult.query.filter_by(request_key=request_key).first()
    if result:
        result.no_of_time_accessed += 1
        result.last_accessed_on = func.now()
        result.last_accessed_from = request.remote_addr
        db.session.commit()

        return {
            'sl_no': result.sl_no,
            'request_key': result.request_key,
            'result_set': result.result_set,
            'series_data': result.series_data,
            'no_of_time_accessed': result.no_of_time_accessed,
            'last_accessed_on': result.last_accessed_on,
            'last_accessed_from': result.last_accessed_from,
            'is_block': result.is_block
        }
    else:
        return None


def save_forecast_to_db(forecast, key, series):
    with app.app_context():
        try:

            forecast_dict = {str(i + 1): float(f) for i, f in enumerate(forecast)}
            series_dict = {str(k): v for k, v in series.to_dict().items()}

            new_result = PredictiveAnalysisResult(
                request_key=key,
                result_set=forecast_dict,
                series_data=series_dict,
                last_accessed_on=datetime.datetime.now(),
            )
            db.session.add(new_result)
            db.session.commit()
        except Exception as e:
            print(f"Error saving to database: {e}")


@app.route('/plot/<key>')
def get_forecast_data(key):
    result = fetch_result_from_db(key)
    if result is not None:
        forecast_data = result['result_set']
        series_data = result['series_data']
        try:
            series = pd.Series(series_data)
            forecast = pd.Series(forecast_data)

            sns.set(style="whitegrid")
            plt.figure(figsize=(14, 7))
            plt.plot(pd.to_datetime(series.index), series.values, label='Demand Qty', color='blue',
                     linestyle='-', linewidth=2)
            plt.plot(pd.date_range(start=pd.to_datetime(series.index).max(), periods=len(forecast), freq='M'),
                     forecast, label='Forecasted Qty', color='red', linestyle='--', linewidth=2)
            plt.title('Demand Quantity and Forecast', fontsize=16)
            plt.xlabel('Date', fontsize=14)
            plt.ylabel('Demand Qty', fontsize=14)
            plt.legend(fontsize=12)
            plt.tight_layout()

            buf = io.BytesIO()
            plt.savefig(buf, format='png')
            buf.seek(0)
            plt.close()
            return send_file(buf, mimetype='image/png')

        except Exception as e:
            return jsonify({'message': 'Data Not formatted correctly'}), 400

    else:
        return jsonify({'message': 'result not found'}), 404


def process_data_from_redis():
    while True:
        keys = redis_client.keys('*')
        for key in keys:
            rawdata = redis_client.get(key)
            request_key = key.decode('utf-8')
            rawdata_str = rawdata.decode('utf-8')

            data = json.loads(rawdata_str)

            proper_data = {
                'demandDate': data["demandDate"],
                'demandQty': data['demandQty'],
            }

            df = pd.DataFrame(proper_data)
            df['demandDate'] = pd.to_datetime(df['demandDate'], format='%d-%m-%Y')
            df.set_index('demandDate', inplace=True)
            df.sort_index(inplace=True)
            df['demandQty'] = df['demandQty'].astype(float)
            series = df['demandQty']
            model = ARIMA(df['demandQty'], order=(5, 1, 0))
            model_fit = model.fit()
            forecast = model_fit.forecast(steps=12)

            # store in postgres
            save_forecast_to_db(forecast, request_key, series)
            # clear redis record
            redis_client.delete(key)
        else:
            time.sleep(1)


if __name__ == '__main__':
    threading.Thread(target=process_data_from_redis, daemon=True).start()
    app.run(port=port_no)
