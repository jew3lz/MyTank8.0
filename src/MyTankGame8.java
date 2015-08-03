import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import java.lang.Thread;

/**
 * Created by JEWELZ on 14/11/15.
 * MyTankGame 8.0
 * 1.绘制坦克√
 * 2.坦克可以移动√
 * 3.可以发射子弹√
 * 4.子弹可以连发(最多连发五颗）√
 * 5.击中敌方坦克时,敌方坦克消失并发生爆炸效果√
 * 6.敌人坦克发射子弹击中我方时,我方坦克爆炸并发生爆炸效果√
 *    6.1.決定把判斷是否碰撞的函數寫到EnemyTank類
 * 7.防止敵人坦克重疊運動√
 *    7.1.做一個開始的Panel,它是一個空的
 *    7.2.闪烁效果
 * 8.可以分關√
 * 9.可以再遊戲時暫停和繼續√
 *    9.1.当用户点击暂停时,子弹的速度和坦克的速度设为0,并让坦克的方向不发生变化
 * 10.可以記錄玩家的成績√
 *    10.1.用文件流的方式
 *    10.2.单写一个记录类,完成对玩家的记录
 *    10.3.先完成保存共击毁了多少辆敌人的坦克的功能
 *    10.4.存盘退出游戏,可以记录当时坦克的坐标,并载入
 * 11.Java如何操作聲音文件√
 *    11.1.对声音文件的操作
 */
public class MyTankGame8 extends JFrame implements ActionListener {



    MyPanel mp=null;
    //定義一個開始面板
    MyStartPanel msp=null;
    //做出需要的菜单
    JMenuBar jmb=null;
    //开始游戏
    JMenu jm1=null;
    JMenuItem jmi1=null;
    JMenuItem jmi2=null;
    JMenuItem jmi3=null;
    JMenuItem jmi4=null;


    //退出系统

    public static void main(String args[])
    {
        MyTankGame8 mtg=new MyTankGame8();
    }
    //构造函数
    public MyTankGame8()
    {
//        mp=new MyPanel();
        //启动MyPanel线程
//        Thread t=new Thread(mp);
//        t.start();

//        this.add(mp);

        //创建菜单及菜单选项
        jmb=new JMenuBar();
        jm1=new JMenu("游戏(G)");
        //设置助记符
        jm1.setMnemonic('G');
        jmi1=new JMenuItem("开始新游戏(N)");
        jmi2=new JMenuItem("退出游戏(E)");
        jmi3=new JMenuItem("存盘退出(C)");
        jmi4=new JMenuItem("接上局玩(S)");
        jmi3.addActionListener(this);
        jmi3.setActionCommand("saveExit");
        jmi2.addActionListener(this);
        jmi2.setActionCommand("exit");
        jmi4.addActionListener(this);
        jmi4.setActionCommand("continue");
        jmi2.setMnemonic('E');
        jmi1.setMnemonic('N');
        //对jmi1响应
        jmi1.addActionListener(this);
        jmi1.setActionCommand("new game");
        jm1.add(jmi1);
        jm1.add(jmi2);
        jmb.add(jm1);
        jm1.add(jmi3);
        jm1.add(jmi4);

        msp=new MyStartPanel();
        Thread t=new Thread(msp);
        t.start();

        this.setJMenuBar(jmb);
        this.add(msp);
        this.setSize(600,500);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);




    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //对用户不同的选择做出不同的处理
        if(e.getActionCommand().equals("new game"))
        {
            //创建游戏界面面板
            mp=new MyPanel("newGame");
            //启动MyPanel线程
            Thread t=new Thread(mp);
            t.start();
            //先删除旧Panel
            this.remove(msp);
            this.add(mp);
            //注册监听
            this.addKeyListener(mp);
            //显示(刷新JFrame)
            this.setVisible(true);

        }else if (e.getActionCommand().equals("exit"))
        {
            //用户点击了退出系统菜单
            //保存击毁敌人数量.
            Recorder.SaveRecord();


            System.exit(0);
        }//对存盘退出做处理
        else if (e.getActionCommand().equals("saveExit"))
        {
            Recorder rd= new Recorder();
            rd.setEts(mp.enemyTanks);
            //保存击毁敌人的数量和敌人的坐标
            rd.SaveRecAndEnemy();


            //退出(0代表正常退出,1代表异常退出)
            System.exit(0);
        }else if (e.getActionCommand().equals("continue"))
        {
            //
            //创建游戏界面面板
            mp=new MyPanel("con");
//            mp.flag="con";

            //启动MyPanel线程
            Thread t=new Thread(mp);
            t.start();
            //先删除旧Panel
            this.remove(msp);
            this.add(mp);
            //注册监听
            this.addKeyListener(mp);
            //显示(刷新JFrame)
            this.setVisible(true);
        }

    }
}
//就是一個提示作用
class MyStartPanel extends JPanel implements Runnable
{

    int times=0;

    public void paint(Graphics g)
    {
        super.paint(g);
        g.fillRect(0,0,400,300);

        if(times%2==0){

            //提示信息
            g.setColor(Color.blue);
            Font myFont;
            myFont = new Font("娃娃體-繁",Font.BOLD,30);
            g.setFont(myFont);
            g.drawString("stage:1",130,140);

        }


    }

    @Override
    public void run() {


        while (true)
        {
            //休眠
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            times++;

            //重绘
            this.repaint();
        }
    }
}

//我的面板
class MyPanel extends JPanel implements KeyListener,Runnable
{

    //定义一个己方坦克(先定义为空，在构造函数里再初始化）
    MyTank myTank=null;

    //判断是续上局,还是新游戏
//    String flag="newGame";

    //定义敌方坦克组
    Vector<EnemyTank> enemyTanks = new Vector<EnemyTank>();
    Vector<Node> nodes =new Vector<Node>();
    int enemySize=3;

    //定义炸弹集合
    Vector<Bomb> bombs=new Vector<Bomb>();

    //播放开战声音
    AePlayWave apw=new AePlayWave("/Users/Jewelz/Desktop/1.mp3");



    //定义三张爆炸图片,三张图片组成一颗炸弹
    Image image1=null;
    //(初始化任务交到构造函数)
    Image image2=null;
    Image image3=null;

    //构造函数
    public MyPanel(String flag)
    {
        apw.start();

        //恢复记录
        Recorder.getRecord();
//        bombs.add(new Bomb(0,0));
        //括号里的是初始位置
        myTank=new MyTank(100,100);

        if(flag.equals("newGame")) {
            //初始化敌方坦克
            for (int i = 0; i < enemySize; i++) {
                //创建一辆敌人的坦克
                EnemyTank et = new EnemyTank((i + 1) * 50, 0);
                et.setColor(1);
                et.setDirect(2);
                //將MyPanel的敵人坦克向量交給該敵人坦克
                et.setEts(enemyTanks);

                //启动敌人的坦克
                Thread t = new Thread(et);
                t.start();
                //给敌人坦克添加一颗子弹
                Bullet b = new Bullet(et.x + 10, et.y + 30, 2);
                //加入敌人坦克
                et.bb.add(b);
                Thread t2 = new Thread(b);
                t2.start();
                //加入
                enemyTanks.add(et);
            }
        }else{

            nodes=new Recorder().getNodesAndEnNums();
            //初始化敌方坦克
            for (int i = 0; i < nodes.size(); i++) {
                Node node=nodes.get(i);
                //创建一辆敌人的坦克
                EnemyTank et = new EnemyTank(node.x, node.y);
                et.setColor(1);
                et.setDirect(node.direct);
                //將MyPanel的敵人坦克向量交給該敵人坦克
                et.setEts(enemyTanks);

                //启动敌人的坦克
                Thread t = new Thread(et);
                t.start();
                //给敌人坦克添加一颗子弹
                Bullet b = new Bullet(et.x + 10, et.y + 30, 2);
                //加入敌人坦克
                et.bb.add(b);
                Thread t2 = new Thread(b);
                t2.start();
                //加入
                enemyTanks.add(et);
            }

        }
        //初始化爆炸图片

//        try {
//            image1= ImageIO.read(new File("/3.png"));
//            image2= ImageIO.read(new File("/2.png"));
//            image3= ImageIO.read(new File("/1.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        image1=Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/3.png"));
        image2=Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/2.png"));
        image3=Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/1.png"));
        bombs.add(new Bomb(0,0));
    }



    //画出提示信息的函数
    public void showInfo(Graphics g)
    {
        //画出提示信息坦克(该坦克只是提示)
        this.drawTank(80,330,g,0,1);
        g.setColor(Color.blue);
        g.drawString(Recorder.getEnNum()+"",110,350);
        this.drawTank(130,330,g,0,0);
        g.setColor(Color.blue);
        g.drawString(Recorder.getMyLife()+"",165,350);

        //画出玩家的总成绩
        g.setColor(Color.BLACK);
        Font f=new Font("娃娃體-繁",Font.BOLD,20);
        g.setFont(f);
        g.drawString("您的總成績",420,30);

        this.drawTank(420,60,g,0,1);

        g.setColor(Color.BLACK);
        g.drawString(Recorder.getAllEnNum()+"",460,80);


    }

    public void paint(Graphics g)
    {
        super.paint(g);
        //画出我的坦克(到时再封装成函数)
        g.setColor(Color.darkGray);
        g.fillRect(0,0,400,300);

        //画出提示信息
        this.showInfo(g);


        //画出自己的坦克
        if(myTank.alive) {
            this.drawTank(myTank.getX(), myTank.getY(), g, myTank.direct, 0);
        }
        //绘制子弹
        //从bb中取出每一颗子弹并绘制（遍历）
        for(int i=0;i<this.myTank.bb.size();i++) {
            //取出一颗子弹
            Bullet myBullet=myTank.bb.get(i);
            //下面是画出一颗子弹
            if (myBullet!= null && myBullet.alive == true) {
                g.draw3DRect(myBullet.x, myBullet.y, 2, 2, false);
            }
            //判断子弹死亡之后从Vector移除
            if(myBullet.alive==false)
            {
                myTank.bb.remove(myBullet);
            }
        }
        //画出炸弹
        for(int i=0;i<bombs.size();i++)
        {
//            System.out.println("bombs.size()="+bombs.size());
            //取出炸弹
            Bomb bomb=bombs.get(i);//不需要强转,因为用了泛型
//            System.out.println("炸弹炸弹炸弹"+bomb);
            if(bomb.life>6)
            {
                //画出最大的效果
                g.drawImage(image3,bomb.x,bomb.y,30,30,this);
            }else if(bomb.life>3)
            {
                g.drawImage(image2,bomb.x,bomb.y,30,30,this);
            }else
            {
                g.drawImage(image1,bomb.x,bomb.y,30,30,this);
            }

            //让bomb的生命值减小
            bomb.lifeDown();
            //如果炸弹值生命值为零，就把炸弹从Vector中去掉
            if(bomb.life==0)
            {
                bombs.remove(bomb);
            }
        }

        //画出敌人的坦克
        for(int i=0;i<enemySize;i++)
        {
            EnemyTank et=enemyTanks.get(i);

            if(et.alive) {
                this.drawTank(et.getX(), et.getY(), g, et.direct, 1);
                //再画出敌人的子弹
                for(int j=0;j<et.bb.size();j++)
                {
                    //取出一颗子弹
                    Bullet enemyBullet=et.bb.get(j);
                    if(enemyBullet.alive)
                    {
                        g.draw3DRect(enemyBullet.x,enemyBullet.y,1,1,false);
                    }else{
                        //如果敌人的坦克死亡就从Vector去掉
                        et.bb.remove(enemyBullet);
                    }
                }
            }
        }

    }

    //判断敌人是否击中我
    public void hitMe()
    {
        //取出每一个敌人的坦克
        for(int i=0;i<this.enemyTanks.size();i++)
        {
            //取出坦克
            EnemyTank et=enemyTanks.get(i);

            //取出每一颗子弹
            for(int j=0;j<et.bb.size();j++)
            {
                //取出子弹
                Bullet enemyBullet=et.bb.get(j);
                if (myTank.alive) {
                    if(this.hitTank(enemyBullet, myTank)){

                    }
                }
            }
        }
    }

    //判断我的子弹是否击中敌人坦克
    public void hitEnemyTank()
    {
        //判断是否击中敌人坦克(遍历)
        for(int i=0;i<myTank.bb.size();i++)
        {
            //取出子弹
            Bullet mb=myTank.bb.get(i);
            //判断子弹是否有效,存活
            if(mb.alive)
            {
                //取出每个敌人坦克,与子弹判断
                for(int j=0;j<enemyTanks.size();j++)
                {
                    //取出坦克
                    EnemyTank et=enemyTanks.get(j);

                    if(et.alive)
                    {
                        if(this.hitTank(mb,et))
                        {
                            //减少敌人数量
                            Recorder.reduceEnNum();
                            //增加我方战绩
                            Recorder.addEnNumRec();
                        }
                    }
                }
            }
        }
    }


    //专门判断子弹是否击中坦克的函数
    public boolean hitTank(Bullet b,Tank et)
    {
        boolean b1=false;

        //判断该坦克的方向
        switch (et.direct)
        {
            //向上和向下范围一样
            case 0:
            case 2:
                if(b.x>=et.x&&b.x<=et.x+20&&b.y>=et.y&&b.y<=et.y+30)
                {
                    //击中1.子弹死亡 2.目标死亡
                    b.alive=false;
                    et.alive=false;
                    b1=true;
                    //创建一颗炸弹,放入Vector中
                    Bomb bomb=new Bomb(et.x,et.y);
                    bombs.add(bomb);
                }
                break;
            case 1:
            case 3:
                if (b.x>=et.x&&b.x<=et.x+30&&b.y>=et.y&&b.y<=et.y+20)
                {
                    //击中
                    b.alive=false;
                    et.alive=false;
                    b1=true;
                    //创建一颗炸弹,放入Vector中
                    Bomb bomb=new Bomb(et.x,et.y);
                    bombs.add(bomb);
                }
                break;
//                default:System.out.println("xxxxxxxxx");
        }

        return b1;
    }

    //绘制坦克方法(扩展)
    public void drawTank(int x,int y,Graphics g,int direct,int type)
    {
        //判断类型
        switch(type)
        {
            case 0:
                g.setColor(Color.cyan);
                break;
            case 1:
                g.setColor(Color.LIGHT_GRAY);
                break;
        }

        //判断方向
        switch(direct)
        {   //向上
            case 0:
                //向上
                //画出左边的矩形
                g.fill3DRect(x, y, 5, 30, false);
                //画出右边的矩形
                g.fill3DRect(x + 15, y, 5, 30, false);
                //画出中间的矩形
                g.fill3DRect(x + 5, y + 5, 10, 20, false);
                //画出圆形
                g.fillOval(x+5,y+10,10,10);
                //画出炮筒
                g.drawLine(x+10, y+15, x+10, y);

                break;
            case 1:
                //向右
                //画出上面的矩形
                g.fill3DRect(x, y, 30, 5, false);
                //画出下面的矩形
                g.fill3DRect(x, y+15, 30, 5, false);
                //画出中间的矩形
                g.fill3DRect(x + 5, y + 5, 20, 10, false);
                //画出圆形
                g.fillOval(x+10,y+5,10,10);
                //画出炮筒
                g.drawLine(x+15, y+10, x+30, y+10);

                break;
            case 2:
                //向下
                //画出上面的矩形
                g.fill3DRect(x, y, 5, 30, false);
                //画出下面的矩形
                g.fill3DRect(x+15, y, 5, 30, false);
                //画出中间的矩形
                g.fill3DRect(x + 5, y + 5, 10, 20, false);
                //画出圆形
                g.fillOval(x+5,y+10,10,10);
                //画出炮筒
                g.drawLine(x+10, y+15, x+10, y+30);

                break;
            case 3:
                //向左
                //画出上面的矩形
                g.fill3DRect(x, y, 30, 5, false);
                //画出下面的矩形
                g.fill3DRect(x, y+15, 30, 5, false);
                //画出中间的矩形
                g.fill3DRect(x + 5, y + 5, 20, 10, false);
                //画出圆形
                g.fillOval(x+10,y+5,10,10);
                //画出炮筒
                g.drawLine(x+15, y+10, x, y+10);

                break;


        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
    //定义a w s d为控制坦克移动的四个键
    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_W)
        {
            //设置我的坦克的方向
            this.myTank.setDirect(0);
            myTank.moveUp();
        }else if(e.getKeyCode()==KeyEvent.VK_D)
        {
            this.myTank.setDirect(1);
            myTank.moveRight();
        }else if(e.getKeyCode()==KeyEvent.VK_S)
        {
            this.myTank.setDirect(2);
            myTank.moveDown();
        }else if(e.getKeyCode()==KeyEvent.VK_A)
        {
            this.myTank.setDirect(3);
            myTank.moveLeft();
        }
            //开火
            if(e.getKeyCode()==KeyEvent.VK_J)
            {
                //只能发射5颗子弹限制
                if(this.myTank.bb.size()<5)
                {
                    this.myTank.shoot();
                }

            }

        this.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
    //为了随时判断,把hitTank函数摆在这里（随时刷新）
    @Override
    public void run() {
        //每隔50ms 重绘制
        while (true)
        {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //判断击中敌人坦克
            this.hitEnemyTank();
            //判断敌人子弹是否击中我方坦克的函数
            this.hitMe();

//            //判断是否需要给坦克加入新的子弹
//            for(int i=0;i<enemyTanks.size();i++)
//            {
//                EnemyTank et=enemyTanks.get(i);
//                if (et.alive)
//                {
//                    if(et.bb.size()<5){
//                        //没有子弹
//                        //添加
//                        System.out.println("et.bb.size()<5="+et.bb.size());
//                        Bullet b=null;
//                        switch (et.direct)
//                        {
//                            case 0:
//                                //创建一颗子弹,把子弹加入到向量
//                                b=new Bullet(et.x+10,et.y,0);
//                                et.bb.add(b);
//                                break;
//                            case 1:
//                                b=new Bullet(et.x+30,et.y+10,1);
//                                et.bb.add(b);
//                                break;
//                            case 2:
//                                b=new Bullet(et.x+10,et.y+30,2);
//                                et.bb.add(b);
//                                break;
//                            case 3:
//                                b=new Bullet(et.x,et.y+10,3);
//                                et.bb.add(b);
//                                break;
//
//                        }
//
//                        //启动子弹线程
//                        Thread t =new Thread(b);
//                        t.start();
//                    }
//                }
//            }

            this.repaint();
        }
    }
}
