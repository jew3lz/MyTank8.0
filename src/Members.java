import javax.sound.sampled.*;
import java.io.FileWriter;
import java.util.Vector;
import java.io.*;
import java.util.zip.ZipEntry;

/**
 * Created by JEWELZ on 14/11/15.
 */

//播放声音的类
class AePlayWave extends Thread
{
    private String filename;
    public AePlayWave(String wavfile)
    {
        filename=wavfile;
    }

    public void run()
    {
        File soundFile=new File(filename);

        AudioInputStream audioInputStream=null;

        try {
            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        AudioFormat format =audioInputStream.getFormat();
        SourceDataLine auline=null;
        DataLine.Info info =new DataLine.Info(SourceDataLine.class, format);

        try {
            auline=(SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        auline.start();
        int nBytesRead=0;
        byte[] abData=new byte[1024];
        try {
            while(nBytesRead !=-1)
            {
                nBytesRead=audioInputStream.read(abData,0,abData.length);
                if(nBytesRead >=0)
                {
                    auline.write(abData,0,nBytesRead);
                }
            }
        }catch (IOException e){
            e.printStackTrace();

        }finally {
            auline.drain();
            auline.close();
        }

    }
}

//记录点类
class Node
{
    int x;
    int y;
    int direct;
    public Node(int x,int y,int direct)
    {
        this.x=x;
        this.y=y;
        this.direct=direct;
    }
}

//记录类,也可以保存玩家的设置
class Recorder
{
    //记录每一关有多少敌人
    private static int enNum=20;
    //设置我有多少可用的坦克
    private static int myLife=3;
    //記錄總共消滅的敵人數量
    private static int allEnNum=0;
    //从文件中恢复记录点
    static Vector<Node>  nodes=new Vector<Node>();


    private static FileWriter fw=null;
    private static BufferedWriter bw=null;
    private static FileReader fr=null;
    private static BufferedReader br=null;


    private  Vector<EnemyTank> ets=new Vector<EnemyTank>();

    public  Vector<EnemyTank> getEts() {
        return ets;
    }

    public  void setEts(Vector<EnemyTank> ets) {
        this.ets = ets;
    }

    //完成读取任务
    public Vector<Node> getNodesAndEnNums()
    {
        try {
            fr=new FileReader("/Users/Jewelz/Desktop/Record.txt");
            br=new BufferedReader(fr);
            String n="";
            //先读取第一行
            n=br.readLine();
            allEnNum=Integer.parseInt(n);
            while((n=br.readLine())!=null)
            {
                String []xyz=n.split(" ");

                Node node = new Node(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2]));
                nodes.add(node);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return nodes;
    }


    //从文件中读取记录
    public static void getRecord()
    {
        try {
            fr=new FileReader("/Users/Jewelz/Desktop/Record.txt");
            br=new BufferedReader(fr);
            String n=br.readLine();
            allEnNum=Integer.parseInt(n);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //保存击毁敌人的数量和敌人坦克的坐标,方向
    public  void SaveRecAndEnemy()
    {
        //创建文件流
        try {
            fw=new FileWriter("/Users/Jewelz/Desktop/Record.txt");
            bw=new BufferedWriter(fw);

            bw.write(allEnNum+"\r\n");

            //保存当前活的敌人坦克坐标和方向
            for(int i=0;i<ets.size();i++)
            {
                //取出第一个坦克
                EnemyTank et=ets.get(i);

                if(et.alive)
                {
                    String rcd=et.x+" "+et.y+" "+et.direct;

                    //写入
                    bw.write(rcd+"\r\n");
                    
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                //后开的先关闭
                bw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    //把玩家击毁敌人坦克数量保存到文件中
    public static void SaveRecord()
    {
        //创建文件流
        try {
            fw=new FileWriter("/Users/Jewelz/Desktop/Record.txt");
            bw=new BufferedWriter(fw);

            bw.write(allEnNum+"\r\n");

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                //后开的先关闭
                bw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static int getAllEnNum() {
        return allEnNum;
    }

    public static void setAllEnNum(int allEnNum) {
        Recorder.allEnNum = allEnNum;
    }



    public static int getEnNum() {
        return enNum;
    }public static void setEnNum(int enNum) {
        Recorder.enNum = enNum;
    }public static int getMyLife() {
        return myLife;
    }public static void setMyLife(int myLife) {
        Recorder.myLife = myLife;
    }

    //减少敌人数
    public static  void reduceEnNum()
    {
        enNum--;
    }
    //消滅敵人時
    public static void addEnNumRec()
    {
        allEnNum++;
    }
}

//炸弹类
class Bomb
{
    //定义炸弹的坐标
    int x,y;
    //炸弹的生命周期
    int life=9;
    boolean alive=true;
    public Bomb(int x,int y)
    {
        this.x=x;
        this.y=y;
    }

    //减少生命值
    public void lifeDown()
    {
        if(life>0)
        {
            life--;
        }else{
            this.alive=false;
        }
    }
}

//子弹类
class Bullet implements Runnable
{
    int x;
    int y;
    int direct;
    int speed=2;
    boolean alive=true;
    public Bullet(int x,int y,int direct)
    {
        this.x=x;
        this.y=y;
        this.direct=direct;
        this.speed=speed;
    }

    @Override
    public void run() {

        while (true)
        {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switch (direct) {
                case 0:
                    //子弹向上
                    y -= speed;
                    break;
                case 1:
                    //子弹向右
                    x += speed;
                    break;
                case 2:
                    //子弹向下
                    y += speed;
                    break;
                case 3:
                    //子弹向左
                    x -= speed;
            }
//            System.out.println("子弹坐标:"+x+","+y);

            //判断子弹是否碰触面板边缘
            if(x<0||x>400||y<0||y>300)
            {
                this.alive=false;
                break;
            }
        }
    }
}

//坦克类
class Tank
{
    boolean alive=true;
    //横坐标
    int x=0;
    //纵坐标
    int y=0;
    //坦克方向 0表示上,1表示右,2表示下,3表示左
    int direct;

    //坦克速度
    int speed=4;

    //坦克颜色
    int color;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getDirect() {
        return direct;
    }public void setDirect(int direct) {
    this.direct = direct;
}

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Tank(int x,int y)
    {
        this.x=x;
        this.y=y;
    }
}

//敌人的坦克,把敌人坦克做成线程类
class EnemyTank extends Tank implements Runnable
{
    //继承父类了,注释掉
//    boolean alive=true;
    int times=0;

    //定義一個向量,可以訪問到MyPanel上所有敵方坦克
    Vector<EnemyTank> ets=new Vector<EnemyTank>();

    //定义一个Vector,可以存放敌人的子弹
    Vector<Bullet> bb=new Vector<Bullet>();
    //敌人添加子弹,应当在刚刚创建坦克和敌人的坦克子弹死亡过后.



    public EnemyTank(int x,int y)
    {
        super(x,y);
    }

    //得到敵人的坦克向量
    public void setEts(Vector<EnemyTank> ee)
    {
        this.ets=ee;
    }

    //判斷是否互相碰撞的函數
    public boolean ContactEnemy()
    {
        boolean b=false;

        switch(this.direct)
        {
            case 0:
                //正在判斷的坦克向上
                //取出所有的敵人坦克
                for(int i=0;i<ets.size();i++)
                {
                    //取出第一個坦克
                    EnemyTank et=ets.get(i);
                    //如果不是自己
                    if(et!=this)
                    {
                        //如果第一人稱坦克向上或者向下
                        if(et.direct==0||et.direct==2)
                        {
                            //判斷一個點
                            if(this.x>=et.x&&this.x<=et.x+20&&this.y>=et.y&&this.y<=et.y+30)
                            {
                                return true;
                            }
                            //判斷另一個點
                            if(this.x+20>=et.x&&this.x<=et.x+20&&this.y>=et.y&&this.y<=et.y+30)
                            {
                                return true;
                            }
                        }
                        if(et.direct==3||et.direct==1)
                        {
                            //判斷一個點
                            if(this.x>=et.x&&this.x<=et.x+30&&this.y>=et.y&&this.y<=et.y+20)
                            {
                                return true;
                            }
                            //判斷另一個點
                            if(this.x+20>=et.x&&this.x+20<=et.x+30&&this.y>=et.y&&this.y<=et.y+20)
                            {
                                return true;
                            }
                        }
                    }
                }
                break;
            case 1:
                //正在判斷的坦克向右
                //取出所有的敵人坦克
                for(int i=0;i<ets.size();i++)
                {
                    //取出第一個坦克
                    EnemyTank et=ets.get(i);
                    //如果不是自己
                    if(et!=this)
                    {
                        //如果第一人稱坦克向上或者向下
                        if(et.direct==0||et.direct==2)
                        {
                            //判斷一個點
                            if(this.x+30>=et.x&&this.x+30<=et.x+20&&this.y>=et.y&&this.y<=et.y+30)
                            {
                                return true;
                            }
                            //判斷另一個點
                            if(this.x+30>=et.x&&this.x+30<=et.x+20&&this.y+20>=et.y&&this.y+20<=et.y+30)
                            {
                                return true;
                            }
                        }
                        if(et.direct==3||et.direct==1)
                        {
                            //判斷一個點
                            if(this.x+30>=et.x&&this.x+30<=et.x+30&&this.y>=et.y&&this.y<=et.y+20)
                            {
                                return true;
                            }
                            //判斷另一個點
                            if(this.x+30>=et.x&&this.x+30<=et.x+30&&this.y+20>=et.y&&this.y+20<=et.y+20)
                            {
                                return true;
                            }
                        }
                    }
                }
                break;
            case 2:
                //正在判斷的坦克向下
                //取出所有的敵人坦克
                for(int i=0;i<ets.size();i++)
                {
                    //取出第一個坦克
                    EnemyTank et=ets.get(i);
                    //如果不是自己
                    if(et!=this)
                    {
                        //如果第一人稱坦克向上或者向下
                        if(et.direct==0||et.direct==2)
                        {
                            //判斷一個點
                            if(this.x>=et.x&&this.x<=et.x+20&&this.y+30>=et.y&&this.y+30<=et.y+30)
                            {
                                return true;
                            }
                            //判斷另一個點
                            if(this.x+20>=et.x&&this.x+20<=et.x+20&&this.y+30>=et.y&&this.y+30<=et.y+30)
                            {
                                return true;
                            }
                        }
                        if(et.direct==3||et.direct==1)
                        {
                            //判斷一個點
                            if(this.x>=et.x&&this.x<=et.x+30&&this.y+30>=et.y&&this.y+30<=et.y+20)
                            {
                                return true;
                            }
                            //判斷另一個點
                            if(this.x+20>=et.x&&this.x+20<=et.x+30&&this.y+30>=et.y&&this.y+30<=et.y+20)
                            {
                                return true;
                            }
                        }
                    }
                }
                break;
            case 3:
                //正在判斷的坦克向左
                //取出所有的敵人坦克
                for(int i=0;i<ets.size();i++)
                {
                    //取出第一個坦克
                    EnemyTank et=ets.get(i);
                    //如果不是自己
                    if(et!=this)
                    {
                        //如果第一人稱坦克向上或者向下
                        if(et.direct==0||et.direct==2)
                        {
                            //判斷一個點
                            if(this.x>=et.x&&this.x<=et.x+20&&this.y>=et.y&&this.y<=et.y+30)
                            {
                                return true;
                            }
                            //判斷另一個點
                            if(this.x>=et.x&&this.x<=et.x+20&&this.y+20>=et.y&&this.y+20<=et.y+30)
                            {
                                return true;
                            }
                        }
                        if(et.direct==3||et.direct==1)
                        {
                            //判斷一個點
                            if(this.x>=et.x&&this.x<=et.x+30&&this.y>=et.y&&this.y<=et.y+20)
                            {
                                return true;
                            }
                            //判斷另一個點
                            if(this.x>=et.x&&this.x<=et.x+30&&this.y+20>=et.y&&this.y+20<=et.y+20)
                            {
                                return true;
                            }
                        }
                    }
                }
                break;

        }

        return b;
    }

    @Override
    public void run() {

        while(true)
        {
            switch(this.direct)
            {
                case 0:
                    //说明坦克正在向上移动
                    for(int i=0;i<30;i++) {
                        if(y>0&&!this.ContactEnemy()) {
                            y -= speed;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1:
                    //向右
                    for(int i=0;i<30;i++) {
                        if(x<350&&!this.ContactEnemy()) {
                            x += speed;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2:
                    //向下
                    for(int i=0;i<30;i++) {
                        if(y<250&&!this.ContactEnemy()) {
                            y += speed;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 3:
                    //向左
                    for(int i=0;i<30;i++) {
                        if(x>0&&!this.ContactEnemy()) {
                            x -= speed;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

            }

            this.times=this.times+6;

            if(times%2==0)
            {
                if(alive)
                {
                    if(bb.size()<5)
                    {
                        Bullet b=null;
                        switch (direct)
                        {
                            case 0:
                                //创建一颗子弹,把子弹加入到向量
                                b=new Bullet(x+10,y,0);
                                bb.add(b);
                                break;
                            case 1:
                                b=new Bullet(x+30,y+10,1);
                                bb.add(b);
                                break;
                            case 2:
                                b=new Bullet(x+10,y+30,2);
                                bb.add(b);
                                break;
                            case 3:
                                b=new Bullet(x,y+10,3);
                                bb.add(b);
                                break;

                        }
                        //启动子弹
                        Thread t=new Thread(b);
                        t.start();
                    }
                }
            }



            //让坦克随机产生新方向
            this.direct=(int)(Math.random()*4);

            //判断敌人是否死亡
            if(this.alive==false)
            {
                //让坦克死亡后退出线程
                break;
            }


        }
    }
}

//我的坦克
class MyTank extends Tank
{
    //子弹
//    Bullet b=null;
    Vector<Bullet> bb=new Vector<Bullet>();
    Bullet b=null;
//    Thread d1=new Thread(b);


    //坦克移动方法
    public void moveUp()
    {
        this.y-=speed;
    }
    public void moveRight()
    {
        this.x+=speed;
    }
    public void moveDown()
    {
        this.y+=speed;
    }
    public void moveLeft()
    {
        this.x-=speed;
    }

    public MyTank(int x,int y)
    {
        super(x,y);

    }

    public void shoot()
    {
        switch (this.direct)
        {
            case 0:
                //创建一颗子弹,把子弹加入到向量
                b=new Bullet(x+10,y,0);
                bb.add(b);
                break;
            case 1:
                b=new Bullet(x+30,y+10,1);
                bb.add(b);
                break;
            case 2:
                b=new Bullet(x+10,y+30,2);
                bb.add(b);
                break;
            case 3:
                b=new Bullet(x,y+10,3);
                bb.add(b);
                break;

        }
        //启动子弹
        Thread t =new Thread(b);
        t.start();
    }
}