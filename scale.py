import pandas as pd
from sklearn.preprocessing import StandardScaler

files = [
    # 'NEON_pressure-air_staPresMean.csv',
    # 'NEON_rel-humidity-buoy-dewTempMean.csv',
    # 'NEON_size-dust-particulate-PM10Median.csv',
    # 'NEON_temp-bio-bioTempMean.csv',
    # 'NEON_wind-2d_windDirMean.csv',
    'Stocks-Germany.txt',
    'Stocks-UK.txt',
    'Stocks-USA.txt',
]

for file in files:
    df = pd.read_csv('dataset/' + file, header=0, names=['date', 'time', 'value', 'ukn'])
    scaler = StandardScaler()
    scaler.fit(df['value'].values.reshape(-1, 1))
    scaled_features = scaler.transform(df['value'].values.reshape(-1, 1))

    scaled_df = pd.DataFrame(scaled_features, index=df.index)
    scaled_df.rename(columns={0: 'value'}, inplace=True)
    scaled_df.fillna(method='bfill', inplace=True)
    scaled_df.to_csv('dataset/' + file[:-4] + '_Scaled.csv', index_label='ts')

    f = open('dataset/' + file[:-4] + '_ScaledMetadata.csv', 'w')
    f.write('mean,sd\n' + str(scaler.mean_[0]) + ',' + str(scaler.scale_[0]))
    f.close()

    del df, scaler, scaled_features, scaled_df
