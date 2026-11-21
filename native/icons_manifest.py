import re,sys
p=sys.argv[1];s=open(p,encoding='utf-8').read()
if 'IconA0' in s: sys.exit(0)
# tira o atalho de abertura da MainActivity (passa para os aliases)
n=len(re.findall(r'<intent-filter>\s*<action android:name="android.intent.action.MAIN"\s*/>\s*<category android:name="android.intent.category.LAUNCHER"\s*/>\s*</intent-filter>',s))
assert n==1,n
s=re.sub(r'<intent-filter>\s*<action android:name="android.intent.action.MAIN"\s*/>\s*<category android:name="android.intent.category.LAUNCHER"\s*/>\s*</intent-filter>','',s)
al=''
for i in range(6):
    ic='@mipmap/ic_launcher' if i==0 else f'@mipmap/ic_a{i}'
    al+=f'''
        <activity-alias android:name=".IconA{i}" android:targetActivity=".MainActivity" android:enabled="{'true' if i==0 else 'false'}" android:exported="true" android:icon="{ic}" android:roundIcon="{ic}_round" android:label="@string/app_name">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity-alias>'''
s=s.replace('</application>',al+'\n    </application>',1)
open(p,'w',encoding='utf-8').write(s)
print('aliases ok')
